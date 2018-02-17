// Copyright (C) 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.ical.iter;

import com.google.ical.values.DateTimeValue;
import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;
import com.google.ical.values.Frequency;
import com.google.ical.values.IcalObject;
import com.google.ical.values.RDateList;
import com.google.ical.values.RRule;
import com.google.ical.values.TimeValue;
import com.google.ical.values.Weekday;
import com.google.ical.values.WeekdayNum;
import com.google.ical.util.Predicate;
import com.google.ical.util.Predicates;
import com.google.ical.util.TimeUtils;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * for calculating the occurrences of an individual RFC 2445 RRULE or groups of
 * RRULES, RDATES, EXRULES, and EXDATES.
 *
 * <h4>Glossary</h4>
 * Period - year|month|day|...<br>
 * Day of the week - an int in [0-6].  See RRULE_WDAY_* in rrule.js<br>
 * Day of the year - zero indexed in [0,365]<br>
 * Day of the month - 1 indexed in [1,31]<br>
 * Month - 1 indexed integer in [1,12]
 *
 * <h4>Abstractions</h4>
 * Generator - a function corresponding to an RRULE part that takes a date and
 *   returns a later (year or month or day depending on its period) within the
 *   next larger period.
 *   A generator ignores all periods in its input smaller than its period.
 * <p>
 * Filter - a function that returns true iff the given date matches the subrule.
 * <p>
 * Condition - returns true if the given date is past the end of the recurrence.
 *
 * <p>All the functions that represent rule parts are stateful.
 *
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class RecurrenceIteratorFactory {

  private static final Logger LOGGER = Logger.getLogger(
      RecurrenceIteratorFactory.class.getName());

  /**
   * given a block of RRULE, EXRULE, RDATE, and EXDATE content lines, parse
   * them into a single recurrence iterator.
   * @param rdata ical text.
   * @param dtStart the date of the first occurrence in timezone tzid, which is
   *   used to fill in optional fields in the RRULE, such as the day of the
   *   month for a monthly repetition when no ther day specified.
   *   Note: this may not be the first date in the series since an EXRULE or
   *     EXDATE might force it to be skipped, but there will be no earlier date
   *     generated by this ruleset.
   * @param strict true if any failure to parse should result in a
   *   ParseException.  false causes bad content lines to be logged and ignored.
   */
  public static RecurrenceIterator createRecurrenceIterator(
      String rdata, DateValue dtStart, TimeZone tzid, boolean strict)
      throws ParseException {
    return createRecurrenceIterable(rdata, dtStart, tzid, strict).iterator();
  }

  public static RecurrenceIterable createRecurrenceIterable(
      String rdata, final DateValue dtStart, final TimeZone tzid,
      final boolean strict)
      throws ParseException {
    final IcalObject[] contentLines = parseContentLines(rdata, tzid, strict);

    return new RecurrenceIterable() {
        public RecurrenceIterator iterator() {
          List<RecurrenceIterator> inclusions =
               new ArrayList<RecurrenceIterator>();
          List<RecurrenceIterator> exclusions =
               new ArrayList<RecurrenceIterator>();
          // always include DTStart
          inclusions.add(new RDateIteratorImpl(
                             new DateValue[] {TimeUtils.toUtc(dtStart, tzid)}));
          for (IcalObject contentLine : contentLines) {
            try {
              String name = contentLine.getName();
              if ("rrule".equalsIgnoreCase(name)) {
                inclusions.add(createRecurrenceIterator(
                                   (RRule) contentLine, dtStart, tzid));
              } else if ("rdate".equalsIgnoreCase(name)) {
                inclusions.add(
                    createRecurrenceIterator((RDateList) contentLine));
              } else if ("exrule".equalsIgnoreCase(name)) {
                exclusions.add(createRecurrenceIterator(
                                   (RRule) contentLine, dtStart, tzid));
              } else if ("exdate".equalsIgnoreCase(name)) {
                exclusions.add(
                    createRecurrenceIterator((RDateList) contentLine));
              }
            } catch (IllegalArgumentException ex) {
              // bad frequency on rrule or exrule
              if (strict) { throw ex; }
              LOGGER.log(
                  Level.SEVERE,
                  "Dropping bad recurrence rule line: " + contentLine.toIcal(),
                  ex);
            }
          }
          return new CompoundIteratorImpl(inclusions, exclusions);
        }
      };
  }

  /**
   * like {@link #createRecurrenceIterator(String,DateValue,TimeZone,boolean)}
   * but defaults to strict parsing.
   */
  public static RecurrenceIterator createRecurrenceIterator(
      String rdata, DateValue dtStart, TimeZone tzid)
      throws ParseException {
    return createRecurrenceIterator(rdata, dtStart, tzid, true);
  }

  /**
   * create a recurrence iterator from an rdate or exdate list.
   */
  public static RecurrenceIterator createRecurrenceIterator(RDateList rdates) {
    DateValue[] dates = rdates.getDatesUtc();
    Arrays.sort(dates);
    int k = 0;
    for (int i = 1; i < dates.length; ++i) {
      if (!dates[i].equals(dates[k])) { dates[++k] = dates[i]; }
    }
    if (++k < dates.length) {
      DateValue[] uniqueDates = new DateValue[k ];
      System.arraycopy(dates, 0, uniqueDates, 0, k);
      dates = uniqueDates;
    }
    return new RDateIteratorImpl(dates);
  }

  /**
   * create a recurrence iterator from an rrule.
   * @param rrule the recurrence rule to iterate.
   * @param dtStart the start of the series, in tzid.
   * @param tzid the timezone to iterate in.
   */
  public static RecurrenceIterator createRecurrenceIterator(
      RRule rrule, DateValue dtStart, TimeZone tzid) {
    assert null != tzid;
    assert null != dtStart;

    Frequency freq = rrule.getFreq();
    Weekday wkst = rrule.getWkSt();
    DateValue untilUtc = rrule.getUntil();
    int count = rrule.getCount();
    int interval = rrule.getInterval();
    WeekdayNum[] byDay = rrule.getByDay().toArray(new WeekdayNum[0]);
    int[] byMonth = rrule.getByMonth();
    int[] byMonthDay = rrule.getByMonthDay();
    int[] byWeekNo = rrule.getByWeekNo();
    int[] byYearDay = rrule.getByYearDay();
    int[] bySetPos = rrule.getBySetPos();
    int[] byHour = rrule.getByHour();
    int[] byMinute = rrule.getByMinute();
    int[] bySecond = rrule.getBySecond();

    if (interval <= 0) {  interval = 1; }

    if (null == wkst) {
      wkst = Weekday.MO;
    }

    // Optimize out BYSETPOS where possible.
    if (bySetPos.length != 0) {
      switch (freq) {
        case HOURLY:
          // ;BYHOUR=3,6,9;BYSETPOS=-1,1
          //     is equivalent to
          // ;BYHOUR=3,9
          if (byHour.length != 0 && byMinute.length <= 1
              && bySecond.length <= 1) {
            byHour = filterBySetPos(byHour, bySetPos);
          }
          // Handling bySetPos for rules that are more frequent than daily
          // tends to lead to large amounts of processor being used before other
          // work limiting features can kick in since there many seconds between
          // dtStart and where the year limit kicks in.
          // There are no known use cases for the use of bySetPos with hourly
          // minutely and secondly rules so we just ignore it.
          bySetPos = NO_INTS;
          break;
        case MINUTELY:
          // ;BYHOUR=3,6,9;BYSETPOS=-1,1
          //     is equivalent to
          // ;BYHOUR=3,9
          if (byMinute.length != 0 && bySecond.length <= 1) {
            byMinute = filterBySetPos(byMinute, bySetPos);
          }
          // See bySetPos handling comment above.
          bySetPos = NO_INTS;
          break;
        case SECONDLY:
          // ;BYHOUR=3,6,9;BYSETPOS=-1,1
          //     is equivalent to
          // ;BYHOUR=3,9
          if (bySecond.length != 0) {
            bySecond = filterBySetPos(bySecond, bySetPos);
          }
          // See bySetPos handling comment above.
          bySetPos = NO_INTS;
          break;
        default:
      }
    }

    DateValue start = dtStart;
    if (bySetPos.length != 0) {
      // Roll back till the beginning of the period to make sure that any
      // positive indices are indexed properly.
      // The actual iterator implementation is responsible for anything
      // < dtStart.
      switch (freq) {
        case YEARLY:
          start = dtStart instanceof TimeValue
              ? new DateTimeValueImpl(start.year(), 1, 1, 0, 0, 0)
              : new DateValueImpl(start.year(), 1, 1);
          break;
        case MONTHLY:
          start = dtStart instanceof TimeValue
              ? new DateTimeValueImpl(start.year(), start.month(), 1, 0, 0, 0)
              : new DateValueImpl(start.year(), start.month(), 1);
          break;
        case WEEKLY:
          int d = (7 + wkst.ordinal() - Weekday.valueOf(dtStart).ordinal()) % 7;
          start = TimeUtils.add(dtStart, new DateValueImpl(0, 0, -d));
          break;
        default: break;
      }
    }

    // recurrences are implemented as a sequence of periodic generators.
    // First a year is generated, and then months, and within months, days
    ThrottledGenerator yearGenerator = Generators.serialYearGenerator(
        freq == Frequency.YEARLY ? interval : 1, dtStart);
    Generator monthGenerator = null;
    Generator dayGenerator = null;
    Generator secondGenerator = null;
    Generator minuteGenerator = null;
    Generator hourGenerator = null;

    // When multiple generators are specified for a period, they act as a union
    // operator.  We could have multiple generators (for day say) and then
    // run each and merge the results, but some generators are more efficient
    // than others, so to avoid generating 53 sundays and throwing away all but
    // 1 for RRULE:FREQ=YEARLY;BYDAY=TU;BYWEEKNO=1, we reimplement some of the
    // more prolific generators as filters.
    // TODO(msamuel): don't need a list here
    List<Predicate<? super DateValue>> filters =
      new ArrayList<Predicate<? super DateValue>>();

    switch (freq) {
      case SECONDLY:
        if (bySecond.length == 0 || interval != 1) {
          secondGenerator = Generators.serialSecondGenerator(interval, dtStart);
          if (bySecond.length != 0) {
            filters.add(Filters.bySecondFilter(bySecond));
          }
        }
        break;
      case MINUTELY:
        if (byMinute.length == 0 || interval != 1) {
          minuteGenerator = Generators.serialMinuteGenerator(interval, dtStart);
          if (byMinute.length != 0) {
            filters.add(Filters.byMinuteFilter(byMinute));
          }
        }
        break;
      case HOURLY:
        if (byHour.length == 0 || interval != 1) {
          hourGenerator = Generators.serialHourGenerator(interval, dtStart);
          if (byHour.length != 0) {
            filters.add(Filters.byHourFilter(bySecond));
          }
        }
        break;
      case DAILY:
        break;
      case WEEKLY:
        // week is not considered a period because a week may span multiple
        // months &| years.  There are no week generators, but so a filter is
        // used to make sure that FREQ=WEEKLY;INTERVAL=2 only generates dates
        // within the proper week.
        if (0 != byDay.length) {
          dayGenerator = Generators.byDayGenerator(byDay, false, start);
          byDay = NO_DAYS;
          if (interval > 1) {
            filters.add(Filters.weekIntervalFilter(interval, wkst, dtStart));
          }
        } else {
          dayGenerator = Generators.serialDayGenerator(interval * 7, dtStart);
        }
        break;
      case YEARLY:
        if (0 != byYearDay.length) {
          // The BYYEARDAY rule part specifies a COMMA separated list of days of
          // the year. Valid values are 1 to 366 or -366 to -1. For example, -1
          // represents the last day of the year (December 31st) and -306
          // represents the 306th to the last day of the year (March 1st).
          dayGenerator = Generators.byYearDayGenerator(byYearDay, start);
          break;
        }
        // $FALL-THROUGH$
      case MONTHLY:
        if (0 != byMonthDay.length) {
          // The BYMONTHDAY rule part specifies a COMMA separated list of days
          // of the month. Valid values are 1 to 31 or -31 to -1. For example,
          // -10 represents the tenth to the last day of the month.
          dayGenerator = Generators.byMonthDayGenerator(byMonthDay, start);
          byMonthDay = NO_INTS;
        } else if (0 != byWeekNo.length && Frequency.YEARLY == freq) {
          // The BYWEEKNO rule part specifies a COMMA separated list of ordinals
          // specifying weeks of the year.  This rule part is only valid for
          // YEARLY rules.
          dayGenerator = Generators.byWeekNoGenerator(byWeekNo, wkst, start);
          byWeekNo = NO_INTS;
        } else if (0 != byDay.length) {
          // Each BYDAY value can also be preceded by a positive (n) or negative
          // (-n) integer. If present, this indicates the nth occurrence of the
          // specific day within the MONTHLY or YEARLY RRULE. For example,
          // within a MONTHLY rule, +1MO (or simply 1MO) represents the first
          // Monday within the month, whereas -1MO represents the last Monday of
          // the month. If an integer modifier is not present, it means all days
          // of this type within the specified frequency. For example, within a
          // MONTHLY rule, MO represents all Mondays within the month.
          dayGenerator = Generators.byDayGenerator(
              byDay, Frequency.YEARLY == freq && 0 == byMonth.length, start);
          byDay = NO_DAYS;
        } else {
          if (Frequency.YEARLY == freq) {
            monthGenerator = Generators.byMonthGenerator(
                new int[] { dtStart.month() }, start);
          }
          dayGenerator = Generators.byMonthDayGenerator(
              new int[] { dtStart.day() }, start);
        }
        break;
    }

    if (secondGenerator == null) {
      secondGenerator = Generators.bySecondGenerator(bySecond, start);
    }
    if (minuteGenerator == null) {
      if (byMinute.length == 0 && freq.compareTo(Frequency.MINUTELY) < 0) {
        minuteGenerator = Generators.serialMinuteGenerator(1, dtStart);
      } else {
        minuteGenerator = Generators.byMinuteGenerator(byMinute, start);
      }
    }
    if (hourGenerator == null) {
      if (byHour.length == 0 && freq.compareTo(Frequency.HOURLY) < 0) {
        hourGenerator = Generators.serialHourGenerator(1, dtStart);
      } else {
        hourGenerator = Generators.byHourGenerator(byHour, start);
      }
    }

    if (dayGenerator == null) {
      boolean dailyOrMoreOften = freq.compareTo(Frequency.DAILY) <= 0;
      if (byMonthDay.length != 0) {
        dayGenerator = Generators.byMonthDayGenerator(byMonthDay, start);
        byMonthDay = NO_INTS;
      } else if (byDay.length != 0) {
        dayGenerator = Generators.byDayGenerator(
            byDay, Frequency.YEARLY == freq, start);
        byDay = NO_DAYS;
      } else if (dailyOrMoreOften) {
        dayGenerator = Generators.serialDayGenerator(
            Frequency.DAILY == freq ? interval : 1, dtStart);
      } else {
        dayGenerator = Generators.byMonthDayGenerator(
            new int[] { dtStart.day() }, start);
      }
    }

    if (0 != byDay.length) {
      filters.add(Filters.byDayFilter(byDay, Frequency.YEARLY == freq, wkst));
      byDay = NO_DAYS;
    }

    if (0 != byMonthDay.length) {
      filters.add(Filters.byMonthDayFilter(byMonthDay));
    }

    // generator inference common to all periods
    if (0 != byMonth.length) {
      monthGenerator = Generators.byMonthGenerator(byMonth, start);
    } else if (null == monthGenerator) {
      monthGenerator = Generators.serialMonthGenerator(
          freq == Frequency.MONTHLY ? interval : 1, dtStart);
    }

    // the condition tells the iterator when to halt.
    // The condition is exclusive, so the date that triggers it will not be
    // included.
    Predicate<DateValue> condition;
    boolean canShortcutAdvance = true;
    if (0 != count) {
      condition = Conditions.countCondition(count);
      // We can't shortcut because the countCondition must see every generated
      // instance.
      // TODO(msamuel): if count is large, we might try predicting the end date
      // so that we can convert the COUNT condition to an UNTIL condition.
      canShortcutAdvance = false;
    } else if (null != untilUtc) {
      if ((untilUtc instanceof TimeValue) != (dtStart instanceof TimeValue)) {
        // TODO(msamuel): warn
        if (dtStart instanceof TimeValue) {
          untilUtc = TimeUtils.dayStart(untilUtc);
        } else {
          untilUtc = TimeUtils.toDateValue(untilUtc);
        }
      }
      condition = Conditions.untilCondition(untilUtc);
    } else {
      condition = Predicates.<DateValue>alwaysTrue();
    }

    // combine filters into a single function
    Predicate<? super DateValue> filter;
    switch (filters.size()) {
      case 0:
        filter = Predicates.<DateValue>alwaysTrue();
        break;
      case 1:
        filter = filters.get(0);
        break;
      default:
        filter = Predicates.and(filters);
        break;
    }

    if (false) {
      System.err.println("  start=" + start + "\ndtStart=" + dtStart);
      System.err.println("  yearGenerator=" + yearGenerator);
      System.err.println(" monthGenerator=" + monthGenerator);
      System.err.println("   dayGenerator=" + dayGenerator);
      System.err.println("  hourGenerator=" + hourGenerator);
      System.err.println("minuteGenerator=" + minuteGenerator);
      System.err.println("secondGenerator=" + secondGenerator);
    }

    Generator instanceGenerator = null;
    if (0 != bySetPos.length) {
      instanceGenerator = InstanceGenerators.bySetPosInstanceGenerator(
          bySetPos, freq, wkst, filter,
          yearGenerator, monthGenerator, dayGenerator, hourGenerator,
          minuteGenerator, secondGenerator);
    } else {
      instanceGenerator = InstanceGenerators.serialInstanceGenerator(
          filter, yearGenerator, monthGenerator, dayGenerator,
          hourGenerator, minuteGenerator, secondGenerator);
    }

    return new RRuleIteratorImpl(
        dtStart, tzid, condition, instanceGenerator,
        yearGenerator, monthGenerator, dayGenerator,
        hourGenerator, minuteGenerator, secondGenerator,
        canShortcutAdvance);
  }

  /**
   * a recurrence iterator that returns the union of the given recurrence
   * iterators.
   */
  public static RecurrenceIterator join(
      RecurrenceIterator a, RecurrenceIterator... b) {
    List<RecurrenceIterator> incl = new ArrayList<RecurrenceIterator>();
    incl.add(a);
    incl.addAll(Arrays.asList(b));
    return new CompoundIteratorImpl(
        incl, Collections.<RecurrenceIterator>emptyList());
  }

  /**
   * an iterator over all the dates included except those excluded, i.e.
   * <code>inclusions - exclusions</code>.
   * Exclusions trump inclusions, and {@link DateValue dates} and
   * {@link DateTimeValue date-times} never match one another.
   * @param included non null.
   * @param excluded non null.
   * @return non null.
   */
  public static RecurrenceIterator except(
      RecurrenceIterator included, RecurrenceIterator excluded) {
    return new CompoundIteratorImpl(
        Collections.<RecurrenceIterator>singleton(included),
        Collections.<RecurrenceIterator>singleton(excluded));
  }

  private static final Pattern FOLD = Pattern.compile("(?:\\r\\n?|\\n)[ \t]");
  private static final Pattern NEWLINE = Pattern.compile("[\\r\\n]+");
  private static final Pattern RULE = Pattern.compile(
      "^(?:R|EX)RULE[:;]", Pattern.CASE_INSENSITIVE);
  private static final Pattern DATE = Pattern.compile(
      "^(?:R|EX)DATE[:;]", Pattern.CASE_INSENSITIVE);
  private static IcalObject[] parseContentLines(
      String rdata, TimeZone tzid, boolean strict)
      throws ParseException {
    String unfolded = FOLD.matcher(rdata).replaceAll("").trim();
    if ("".equals(unfolded)) { return new IcalObject[0]; }
    String[] lines = NEWLINE.split(unfolded);
    IcalObject[] out = new IcalObject[lines.length];
    int nbad = 0;
    for (int i = 0; i < lines.length; ++i) {
      String line = lines[i].trim();
      try {
        if (RULE.matcher(line).find()) {
          out[i] = new RRule(line);
        } else if (DATE.matcher(line).find()) {
          out[i] = new RDateList(line, tzid);
        } else {
          throw new ParseException(lines[i], i);
        }
      } catch (ParseException ex) {
        if (strict) {
          throw ex;
        }
        LOGGER.log(Level.SEVERE,
                   "Dropping bad recurrence rule line: " + line, ex);
        ++nbad;
      } catch (IllegalArgumentException ex) {
        if (strict) {
          throw ex;
        }
        LOGGER.log(Level.SEVERE,
                   "Dropping bad recurrence rule line: " + line, ex);
        ++nbad;
      }
    }
    if (0 != nbad) {
      IcalObject[] trimmed = new IcalObject[out.length - nbad];
      for (int i = 0, k = 0; i < trimmed.length; ++k) {
        if (null != out[k]) { trimmed[i++] = out[k]; }
      }
      out = trimmed;
    }
    return out;
  }

  /**
   * Given an array like BYMONTH=2,3,4,5 and a set pos like BYSETPOS=1,-1
   * reduce both clauses to a single one, BYMONTH=2,5 in the preceding.
   */
  private static int[] filterBySetPos(int[] members, int[] bySetPos) {
    members = Util.uniquify(members);
    IntSet iset = new IntSet();
    for (int pos : bySetPos) {
      if (pos == 0) { continue; }
      if (pos < 0) {
        pos += members.length;
      } else {
        --pos;  // Zero-index.
      }
      if (pos >= 0 && pos < members.length) {
        iset.add(members[pos]);
      }
    }
    return iset.toIntArray();
  }

  private static final int[] NO_INTS = new int[0];
  private static final WeekdayNum[] NO_DAYS = new WeekdayNum[0];

  private RecurrenceIteratorFactory() {
    // uninstantiable
  }

}

package net.mortalsilence.indiepim.server.controller;

import net.mortalsilence.indiepim.server.calendar.synchronisation.CalSynchroService;
import net.mortalsilence.indiepim.server.controller.dto.GetMessageRequest;
import net.mortalsilence.indiepim.server.dto.*;
import net.mortalsilence.indiepim.server.calendar.ICSParser;
import net.mortalsilence.indiepim.server.command.actions.*;
import net.mortalsilence.indiepim.server.command.exception.CommandException;
import net.mortalsilence.indiepim.server.command.handler.*;
import net.mortalsilence.indiepim.server.command.results.*;
import net.mortalsilence.indiepim.server.dao.CalendarDAO;
import net.mortalsilence.indiepim.server.dao.MessageDAO;
import net.mortalsilence.indiepim.server.dao.UserDAO;
import net.mortalsilence.indiepim.server.domain.*;
import net.mortalsilence.indiepim.server.message.MessageConstants;
import net.mortalsilence.indiepim.server.utils.CalendarUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: AmIEvil
 * Date: 19.08.13
 * Time: 22:04
 * To change this template use File | Settings | File Templates.
 */
@RestController
@RequestMapping("/api/command")
public class CommandController {

    // TODO move ActionHandlers to registry!!!
    @Inject private GetMessageAccountsHandler getMessageAccountsHandler;
    @Inject private GetAllMessagesHandler getMessagesHandler;
    @Inject private GetMessageHandler getMessageHandler;
    @Inject private CreateOrUpdateMessageAccountHandler createMsgAccountHandler;
    @Inject private CreateOrUpdateUserHandler createUserHandler;
    @Inject private GetUsersHandler getUsersHandler;
    @Inject private UserDAO userDAO;
    @Inject private ICSParser icsParser;
    @Inject private MarkMessageAsReadHandler markReadHandler;
    @Inject private GetMessageStatsHandler messageStatsHandler;
    @Inject private GetEmailAddressesHandler getEmailAddressHandler;
    @Inject private SendMessageHandler sendMessageHandler;
    @Inject private GetTagsHandler tagsHandler;
    @Inject private StartAccountSynchronisationHandler accountSyncHandler;
    @Inject private SendChatMessageHandler chatMessageHandler;
    @Inject private DeleteMessagesHandler deleteMessagesHandler;
    @Inject private GetAttachmentHandler attachmentHandler;
    @Inject private CreateDraftHandler createDraftHandler;
    @Inject private MessageDAO messageDAO;
    @Inject private CalendarDAO calendarDAO;
    @Inject private GetCalendarsHandler getCalendarsHandler;
    @Inject private CalSynchroService calSyncService;
    @Inject private CalendarUtils calUtils;
    @Inject private CreateOrUpdateCalendarHandler createOrUpdateCalendarHandler;
    @Inject private DeleteCalendarHandler deleteCalendarHandler;
    @Inject private CreateOrUpdateEventHandler createOrUpdateEventHandler;
    @Inject private DeleteEventHandler deleteEventHandler;
    @Inject private ActionUtils actionUtils;

    @RequestMapping(value="getMessageAccounts", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public List<MessageAccountDTO> getMessageAccounts() {
        return getMessageAccountsHandler.execute(new GetMessageAccounts()).getAccounts();
    }

    @RequestMapping(value="getMessages", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    @ResponseBody
    public MessageListResult getMessages(@RequestBody GetMessageRequest request) {
        Long offset = request.getOffset();
        if(offset == null || offset < 0)
            offset = 0L;
        Integer pageSize = request.getPageSize();
        if(pageSize == null || pageSize < 0)
            pageSize = 50;
        return getMessagesHandler.execute(new GetMessages(offset, pageSize, request.getAccountId(), request.getTagName(),
                request.getTagLineageId(), request.getSearchTerm(), request.getRead()));
    }

    @RequestMapping(value="getMessage/{messageId}", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public MessageDTO getMessage(@PathVariable(value = "messageId") final Long messageId) throws CommandException {
        final MessageDTOResult result = getMessageHandler.execute(new GetMessage(messageId));
        return result.getMessageDTO();
    }

    @RequestMapping(value="createDraft", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Object createDraft (@RequestParam(value = "origMessageId", required = false) final Long origMessageId){
        try {
            return createDraftHandler.execute(new CreateDraft(origMessageId));
        } catch (CommandException e) {
            return new ErrorResult(e.getMessage());
        }
    }

    @RequestMapping(value="markAsRead/{messageId}", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public List<MessageDTO> markMessageRead(@PathVariable(value = "messageId") final Long messageId,
                                  @RequestParam(value = "read", required = false) final Boolean readFlag) {
        final List<Long> ids = new LinkedList<>();
        final Boolean read = readFlag != null ? readFlag : Boolean.TRUE;
        ids.add(messageId);
        final MessageDTOListResult result = markReadHandler.execute(new MarkMessagesAsRead(ids, read));
        return result.getMessages();
    }

    @RequestMapping(value="sendChatMessage/{userId}", consumes = "text/plain", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public BooleanResult sendChatMessage(@PathVariable(value = "userId") final Long userId,
                                  @RequestBody final String message) {
        return chatMessageHandler.execute(new SendChatMessage(userId, message));
    }

    @RequestMapping(value="createOrUpdateUser", consumes = "application/json", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public IdResult createUser(@RequestBody final UserDTO user) {
       return createUserHandler.execute(new CreateOrUpdateUser(user));
    }

    @RequestMapping(value="getUsers", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Collection<UserDTO> getUsers(@RequestParam(value="onlineOnly", required = false) final Boolean onlineOnly) {
        final GetUsers action = onlineOnly != null ? new GetUsers(onlineOnly) : new GetUsers();
        return getUsersHandler.execute(action).getUsers();
    }

    @RequestMapping(value="createOrUpdateMessageAccount",  consumes = "application/json", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public IdVersionResult createOrUpdateMessageAccount(@RequestBody final MessageAccountDTO accountDTO){
        return createMsgAccountHandler.execute(new CreateOrUpdateMessageAccount(accountDTO));
    }

    @RequestMapping(value="deleteMessageAccount", consumes = "application/json", produces = "application/json;charset=UTF-8")
    public void deleteMessageAccount(@RequestBody final MessageAccountDTO accountDTO) {
        // TODO
    }

    @RequestMapping(value="syncMessageAccount/{accountId}", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public BooleanResult syncMessageAccount(@PathVariable(value = "accountId") final Long accountId,
                                     @RequestParam(value ="full", required = false) final Boolean fullSync) {
        return accountSyncHandler.execute(new StartAccountSynchronisation(accountId, fullSync));
    }

    @RequestMapping(value="importics", consumes = "multipart/form-data", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String importIcs(@RequestParam("file") CommonsMultipartFile upload) {
        final UserPO user = userDAO.getUser(actionUtils.getUserId());
        try {
            final CalendarPO newCalendar = icsParser.updateCalFromIcalInputStream(user, null, upload.getInputStream(), upload.getName());
            int imported = newCalendar.getEvents().size();
            return "<h3>Import successful</h3><p>Imported " + imported + " events.";
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @RequestMapping(value="uploadAttachment", consumes = "multipart/form-data", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Object uploadAttachment(@RequestParam("file") CommonsMultipartFile attachment,
                                   @RequestParam("messageId") Long messageId) {

        if(attachment.getOriginalFilename() == null)
            throw new IllegalArgumentException("Attachment has no name!");
        InputStream is = null;
        OutputStream os = null;
        try {
            final File tmpFile = File.createTempFile(attachment.getOriginalFilename(), ".tmp");
            os = new FileOutputStream(tmpFile);
            is = attachment.getInputStream();
            IOUtils.copy(is, os);
            final AttachmentPO attachmentPO = new AttachmentPO();
            final UserPO user = userDAO.getUser(actionUtils.getUserId());
            final MessagePO msg = messageDAO.getMessageByIdAndUser(messageId, user.getId());
            if(msg == null)
                throw new IllegalArgumentException("Message with id " + messageId + " not found.");
            attachmentPO.setUser(user);
            attachmentPO.setDisposition(MessageConstants.DISPOSITION_ATTACHMENT);
            attachmentPO.setFilename(attachment.getOriginalFilename());
            attachmentPO.setMimeType(attachment.getContentType());
            attachmentPO.setMessage(msg);
            attachmentPO.setTempFilename(tmpFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(is != null) {
                try { is.close();} catch(IOException ignored) {}
            }
            if(os != null) {
                try {os.close();} catch(IOException ignored) {}
            }
        }
        return "{\"result\":\"OK\"}";
    }

    @RequestMapping(value="getCalendars", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Object getCalendars() {
        return getCalendarsHandler.execute(new GetCalendars()).getCalendars();
    }

    @RequestMapping(value="getCalendar/{id}", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Object getCalendars(@PathVariable("id") final Long calendarId) {
        final Collection<Long> calIds = new LinkedList<>();
        calIds.add(calendarId);
        List<CalendarDTO> calendars = getCalendarsHandler.execute(new GetCalendars(calIds)).getCalendars();
        if(calendars == null || calendars.isEmpty())
            throw new RuntimeException("Calendar with id " + calendarId + " not found!");
        return calendars.get(0);
    }


    @RequestMapping(value="syncCalendar/{id}", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Object syncCalendar(@PathVariable("id") final Long calendarId){
        final CalendarPO cal = calendarDAO.getCalendarById(actionUtils.getUserId(), calendarId);
        if(cal == null)  {
            throw new RuntimeException("Calendar with id " + calendarId + " not found.");
        }
        calSyncService.syncExternalCalendar(cal);
        return "{\"result\":\"OK\"}";
    }


    @RequestMapping(value="addExternalCalendar", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Object addExternalCalendar(@RequestParam("url") final String url,
                                      @RequestParam(value = "username", required = false) final String username,
                                      @RequestParam(value = "password", required = false) final String password) {
        final CalendarDTO calendarDTO = new CalendarDTO();
        calendarDTO.syncUrl = url;
        calendarDTO.userName = username;
        if(password != null && !password.isEmpty())
            calendarDTO.password = password;
        return createOrUpdateCalendarHandler.execute(new CreateOrUpdateCalendar(calendarDTO)).getId();
    }

    @RequestMapping(value="createOrUpdateCalendar", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Long createOrUpdateCalendar(@RequestBody final CalendarDTO calendarDTO) {
        return createOrUpdateCalendarHandler.execute(new CreateOrUpdateCalendar(calendarDTO)).getId();
    }

    @RequestMapping(value="deleteCalendar/{id}", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Object deleteCalendar(@PathVariable("id") final Long calendarId) {
        return deleteCalendarHandler.execute(new DeleteCalendar(calendarId)).getResult();
    }

    @RequestMapping(value="getEvents", produces = "application/json;charset=UTF-8")
    @ResponseBody
    /*
     * start milliseconds from unix epoch
     * end milliseconds from unix epoch
     */
    public Object getEvents(@RequestParam("start") final Long start,
                            @RequestParam("end") final Long end) {

        final List<EventPO> events = calendarDAO.getEvents(actionUtils.getUserId(), new Timestamp(start), new Timestamp(end));
        if(events == null)
            return null;
         return calUtils.mapEventPO2EventDTOList(events);
    }

    @RequestMapping(value="createOrUpdateEvent", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Long createOrUpdateEvent(@RequestBody final EventDTO eventDTO) {
        return createOrUpdateEventHandler.execute(new CreateOrUpdateEvent(eventDTO)).getId();
    }

    @RequestMapping(value="deleteEvent/{id}", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Object deleteEvent(@PathVariable("id") final Long eventId) {
        return deleteEventHandler.execute(new DeleteEvent(eventId)).getResult();
    }


    @RequestMapping(value="getMessageStats", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Object getMessageStats(@RequestParam(value = "type") final String statsTypeStr) {
        try {
            final GetMessageStats.STATS_TYPE statsType = GetMessageStats.STATS_TYPE.valueOf(statsTypeStr);
            // TODO generalize
            return messageStatsHandler.execute(new GetMessageStats(statsType)).getLastTenDaysCount();
        } catch(IllegalArgumentException iae) {
            return null;
        }
    }

    @RequestMapping(value="getEmailAddresses", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public List<EmailAddressDTO> getEmailAddresses (@RequestParam(value="query", required=false) final String query) {
        return getEmailAddressHandler.execute(new GetEmailAddresses(query)).getAddresses();
    }

    /* Encoding for the message to send is explicitly not specified here (may differ from UTF-8)
       -> hopefully handled by Spring MVC and converted to UTF-8 */
    @RequestMapping(value="sendMessage", consumes = "application/json", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Object sendMessage (@RequestBody final SendMessage sendMessageAction) {
        return sendMessageHandler.execute(sendMessageAction);
    }

    @RequestMapping(value="getTags", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Collection<TagDTO> searchForTags(@RequestParam(value="tagLineageId", required = false) final Long tagLinageId,
                                            @RequestParam(value="query", required = false) final String query) {
        return tagsHandler.execute(new GetTags(tagLinageId, query)).getTags();
    }

    @RequestMapping(value="deleteMessage/{id}", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Object deleteMessage(@PathVariable(value = "id") final Long msgId) {
        final List<Long> msgIds = new LinkedList<>();
        msgIds.add(msgId);
        return deleteMessagesHandler.execute(new DeleteMessages(msgIds));
    }

    @RequestMapping(value="getAttachment/{id}")
    @ResponseBody
    public void getAttachment(@PathVariable(value = "id") final Long attachmentId, HttpServletResponse response) throws CommandException {
        try {
            final AttachmentPO attachment = messageDAO.getAttachmentById(attachmentId, actionUtils.getUserId());
            // TODO make the following line configurable. Leaving it out it makes the browser opening it directly (no download dialog),
            // but the filename information will be lost.
            response.setHeader("Content-Disposition", "attachment;filename=\"" + attachment.getFilename() + "\"");

            response.setHeader("Content-Type", attachment.getMimeType());
            final boolean success = attachmentHandler.execute(new GetAttachment(attachmentId, response.getOutputStream())).getResult();
            if(success) {
                return;
            } else {
                throw new CommandException("Could not download attachment from mail server. ");
            }
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @RequestMapping(value="reIndexAllMessages")
    public void reindexAllMessages() {
        messageDAO.reindexAllMessages();
    }
}

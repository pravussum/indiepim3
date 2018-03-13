package net.mortalsilence.indiepim.server.google.contacts.model;

import com.google.api.client.util.Key;
import net.mortalsilence.indiepim.server.message.EmailAddressType;
import net.mortalsilence.indiepim.server.message.MessageConstants;

/**
 * Created with IntelliJ IDEA.
 * User: AmIEvil
 * Date: 11.11.12
 * Time: 13:20
 * To change this template use File | Settings | File Templates.
 */
public class GoogleContactEmailAddress implements MessageConstants {

    protected static final String EMAIL_REL_HOME = "http://schemas.google.com/g/2005#home";
    protected static final String EMAIL_REL_OTHER = "http://schemas.google.com/g/2005#other";
    protected static final String EMAIL_REL_WORK = "http://schemas.google.com/g/2005#work";

    @Key("@address")
    public String emailAddress;

    @Key("@displayName")
    public String displayName;

    @Key("@label")
    public String label;

    @Key("@rel")
    public String type;

    @Key("@primary")
    public Boolean isPrimary;

    /**
     * Converts a Google phone number rel type to our phone number type
     */
    public EmailAddressType convertEmailAddressType() {
        if(type == null)
            return EmailAddressType.OTHER;
        if(type.equals(EMAIL_REL_HOME))
            return EmailAddressType.HOME;
        if(type.equals(EMAIL_REL_WORK))
            return EmailAddressType.WORK;
        if(type.equals(EMAIL_REL_OTHER))
            return EmailAddressType.OTHER;

        /* Defaults to OTHER */
        return EmailAddressType.OTHER;
    }
}

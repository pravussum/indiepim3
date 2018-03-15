package net.mortalsilence.indiepim.server.message.synchronisation;

import com.sun.mail.imap.IMAPFolder;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.mail.FetchProfile;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import java.util.HashMap;
import java.util.Map;

@Component
public class ImapMessageFetcher {

    private static final Logger logger = Logger.getLogger("net.mortalsilence.indiepim");

    public Map<Long, Message> getMessagesWithUid(IMAPFolder folder, boolean fetchFlagsOnly) throws MessagingException {
        Message[] messages;

        long time = System.currentTimeMillis();
        messages = folder.getMessages();
        if(logger.isDebugEnabled())
            logger.debug("IMAPFolder.getContacts() took " + (System.currentTimeMillis() - time) + "ms.");

        FetchProfile fp = new FetchProfile();
        fp.add(UIDFolder.FetchProfileItem.UID);
        if(fetchFlagsOnly) {
            logger.debug("Using Fetch Profile UID + FLAGS");
            fp.add(IMAPFolder.FetchProfileItem.FLAGS);
        }
        time = System.currentTimeMillis();
        folder.fetch(messages, fp);
        if(logger.isDebugEnabled())
            logger.debug("IMAPFolder.fetch() UIDs took " + (System.currentTimeMillis() - time) + "ms.");

        Map<Long, Message>uidMsgMap = new HashMap<>();
        // TODO change everything to IMAP
        time = System.currentTimeMillis();
        for (Message message : messages) uidMsgMap.put(folder.getUID(message), message);
        if(logger.isDebugEnabled())
            logger.debug("Reading UIDs took " + (System.currentTimeMillis() - time) + "ms.");

        return uidMsgMap;
    }
}

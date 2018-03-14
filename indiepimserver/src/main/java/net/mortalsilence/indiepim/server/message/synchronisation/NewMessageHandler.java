package net.mortalsilence.indiepim.server.message.synchronisation;

import com.google.common.collect.Lists;
import com.sun.mail.imap.IMAPFolder;
import net.mortalsilence.indiepim.server.PushMessageService;
import net.mortalsilence.indiepim.server.pushmessage.AccountSyncProgressMessage;
import net.mortalsilence.indiepim.server.domain.MessageAccountPO;
import net.mortalsilence.indiepim.server.domain.TagLineagePO;
import net.mortalsilence.indiepim.server.domain.UserPO;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.mail.FetchProfile;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class NewMessageHandler {

    private final static Logger logger = Logger.getLogger("net.mortalsilence.indiepim");

    private final PersistenceHelper persistenceHelper;
    private final PushMessageService pushMessageService;

    @Inject
    public NewMessageHandler(PersistenceHelper persistenceHelper,
                             PushMessageService pushMessageService) {
        this.persistenceHelper = persistenceHelper;
        this.pushMessageService = pushMessageService;
    }

    public boolean handleNewMessages(final Map<Long, Message> uidMsgMap,
                                     List<Long> uids,
                                     MessageAccountPO account,
                                     final IMAPFolder folder,
                                     TagLineagePO tagLineage,
                                     IncomingMessageHandler updateHandler,
                                     IncomingMessageHandler persistHandler,
                                     IncomingMessageHandler addressHandler,
                                     Session session,
                                     UserPO user, Set<String> hashCache) {

        boolean newMessages = false;
        long cometEventTime = System.currentTimeMillis();

        List<List<Long>> uidSubLists = Lists.partition(uids, 50);
        int i = 0;
        for(List<Long> uidSubList : uidSubLists) {
            try {
                Message[] msgContainer = folder.getMessagesByUID(uidSubList.stream().mapToLong(uid -> uid).toArray());
                FetchProfile fp = new FetchProfile();
                fp.add(IMAPFolder.FetchProfileItem.MESSAGE);
                long fetchStart = System.currentTimeMillis();
                folder.fetch(msgContainer, fp);
                logger.debug("Fetch of " + uidSubList.size() + " messages took " + (System.currentTimeMillis() - fetchStart) + " ms.");
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }

            for (Long anUidSubList : uidSubList) {
                /* Update clients every second */
                i++;
                if (System.currentTimeMillis() - cometEventTime > 1000) {
                    pushMessageService.sendMessage(account.getUser().getId(), new AccountSyncProgressMessage(account.getUser().getId(), account.getId(), folder.getFullName(), uids.size(), i));
                    cometEventTime = System.currentTimeMillis();
                }
                final Message message = uidMsgMap.get(anUidSubList);

                try {
                    newMessages |= persistenceHelper.persistMessage(account,
                            folder,
                            tagLineage,
                            updateHandler,
                            persistHandler,
                            addressHandler,
                            session,
                            user,
                            anUidSubList,
                            message,
                            hashCache);
                } catch (Exception e) {
                    // log and do not fail the whole sync because of one invalid message
                    logger.error("Persisting message " + anUidSubList + " in folder " + folder.getName() + " failed.", e);
                }
            }
        }
        return newMessages;
    }
}

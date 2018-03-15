package net.mortalsilence.indiepim.server.message.synchronisation;

import net.mortalsilence.indiepim.server.dao.MessageDAO;
import net.mortalsilence.indiepim.server.domain.MessageAccountPO;
import net.mortalsilence.indiepim.server.domain.MessagePO;
import net.mortalsilence.indiepim.server.domain.TagLineagePO;
import net.mortalsilence.indiepim.server.domain.UserPO;
import net.mortalsilence.indiepim.server.message.ConnectionUtils;
import net.mortalsilence.indiepim.server.message.SyncUpdateMethod;
import net.mortalsilence.indiepim.server.utils.MessageUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

@Service
/*
 * Helper class to fulfill certain task in their own new transaction and comit it immediately
 * without interferring with the surrounding transaction
 * This is primarily useful for persisting single messages in a long running account sync transaction.
 * A rollback for a single message transaction shouldn't affect the whole sync process.
 * The reason we didn't use nested transactions here is that a persisted message should immediately be
 * visible in the message list, since a account sync can take very long. Furthermore are nested transactions
 * only commited at the very end of the surrounding transaction an thus lost, if the sync process is canceled or rolled
 * back.
 */
public class PersistenceHelper {

    private final static Logger logger = Logger.getLogger("net.mortalsilence.indiepim");
    private final MessageUtils messageUtils;
    private final MessageDAO messageDAO;
    private final ConnectionUtils connectionUtils;
    private final IncomingMessageHandlerFactory incomingMessageHandlerFactory;
    private final PersistMessageHandler persistMessageHandler;
    private final MailAddressHandler mailAddressHandler;

    @Inject
    public PersistenceHelper(MessageUtils messageUtils,
                             MessageDAO messageDAO,
                             ConnectionUtils connectionUtils,
                             IncomingMessageHandlerFactory incomingMessageHandlerFactory,
                             PersistMessageHandler persistMessageHandler,
                             MailAddressHandler mailAddressHandler) {
        this.messageUtils = messageUtils;
        this.messageDAO = messageDAO;
        this.connectionUtils = connectionUtils;
        this.incomingMessageHandlerFactory = incomingMessageHandlerFactory;
        this.persistMessageHandler = persistMessageHandler;
        this.mailAddressHandler = mailAddressHandler;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    /* made public for transactional annotation to work */
    // TODO: the messsage handle method return the changed messagePO! Is this used after calling persistMessage?
    public boolean persistMessage(final MessageAccountPO account,
                                  final Folder folder,
                                  final TagLineagePO tagLineage,
                                  final Session session,
                                  final UserPO user,
                                  final Long msgUid,
                                  final Message message,
                                  final Set<String> hashCache,
                                  SyncUpdateMethod updateMode) {
        boolean isNew = false;
        try {
            if(logger.isInfoEnabled())
                logger.info("MSG_UID: " + msgUid);
            String senderStr = messageUtils.getSenderStr(message);
            if(senderStr == null)
                senderStr = "";
            final String hash = messageUtils.getHash(senderStr,
                    messageUtils.getReceiverStr(message),
                    message.getSubject(),
                    message.getReceivedDate());
            if(hashCache.contains(hash) || hasDuplicate(account, hash)) {
                /* Update existing message in different folder */
                MessagePO duplicate = getDuplicate(account, hash);
                if(!duplicate.hasTagLineage(tagLineage, msgUid)){
                    messageDAO.addTagLineage(duplicate, tagLineage, msgUid);
                }
                // TODO: replace by something cool
                IncomingMessageHandler updateHandler = incomingMessageHandlerFactory.getIncomingMessageHandler(updateMode);
                if(updateHandler != null)
                    duplicate = updateHandler.handleMessage(message, duplicate, msgUid, account, folder, tagLineage, session, user);
            } else {
                /* handle new messages */
                MessagePO newMessage = new MessagePO();
                newMessage.setHash(hash);
                newMessage = persistMessageHandler.handleMessage(message, newMessage, msgUid, account, folder, tagLineage, session, user);
                newMessage = mailAddressHandler.handleMessage(message, newMessage, msgUid, account, folder, tagLineage, session, user);
                isNew = true;
            }
            hashCache.add(hash);
        } catch (Exception e) {
            String folderName;
            try {
                folderName = folder.getFullName();
            } catch(Exception e2){
                folderName = "<Could not get message folder name>";
            }
            logger.error("Error synchronizing message " + msgUid + " in folder " + folderName, e);
        }
        return isNew;
    }

    /**
     * This helper method is to allow tag lineage creation to happen in an own immediately commited transaction
     * Otherwise, DB locks would be held for the newly created rows which would cause a deadlock during message
     * persistence, since these transactions are completely independent and the outer transaction (holding the lock)
     * is suspended in the meanwhile.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public TagLineagePO getOrCreateTagLineage(final MessageAccountPO account, String folderFullName, char folderSeparator) {
   		return connectionUtils.getOrCreateTagLineage(account, folderFullName, folderSeparator);
   	}

    @Transactional(propagation = Propagation.REQUIRED)
    public Date updateLastSyncRun(final UserPO user, final long accountId) {
        final MessageAccountPO account = messageDAO.getMessageAccount(user.getId(), accountId);
        final Date lastSyncRun = Calendar.getInstance().getTime();
        account.getMessageAccountStats().setLastSyncRun(lastSyncRun);
        return lastSyncRun;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void markMessageAsRead(final Long userId, final Long msgId, final Long tagLineageId, final Boolean readFlag) {
        messageDAO.getMessageByUIDAndTagLineage(userId, msgId, tagLineageId).setRead(readFlag);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public int deleteMessagesForTagLineage(final Long userId, final Collection<Long> msgIds, Long tagLineageId) {
        return messageDAO.deleteMessagesForTagLineage(userId, msgIds, tagLineageId);
    }

    private MessagePO getDuplicate(final MessageAccountPO account, final String hash)  {
   		return messageDAO.getMessageFromHash(account.getUser().getId(), account.getId(), hash);
   	}

   	private Boolean hasDuplicate(final MessageAccountPO account, final String hash) {
   		return messageDAO.existsMessageWithHash(account.getUser().getId(), account.getId(), hash);
   	}

}

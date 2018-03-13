package net.mortalsilence.indiepim.server.message.synchronisation;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPFolder.FetchProfileItem;
import net.mortalsilence.indiepim.server.comet.AccountSyncProgressMessage;
import net.mortalsilence.indiepim.server.comet.AccountSyncedMessage;
import net.mortalsilence.indiepim.server.comet.CometService;
import net.mortalsilence.indiepim.server.comet.NewMsgMessage;
import net.mortalsilence.indiepim.server.dao.MessageDAO;
import net.mortalsilence.indiepim.server.dao.TagDAO;
import net.mortalsilence.indiepim.server.domain.*;
import net.mortalsilence.indiepim.server.message.ConnectionUtils;
import net.mortalsilence.indiepim.server.message.MessageConstants;
import net.mortalsilence.indiepim.server.message.SyncUpdateMethod;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.mail.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class MsgSynchroService implements MessageConstants {

	final static Logger logger = Logger.getLogger("net.mortalsilence.indiepim");
	private static final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<String, ReentrantLock>(); 
	
	private static final Set<String> hashCache = new HashSet<>();
    private final MessageDAO messageDAO;
    private final TagDAO tagDAO;
    private final ConnectionUtils connectionUtils;
    private final PersistMessageHandler persistMessageHandler;
    private final UpdateFlagsAndFoldersHandler updateFlagsAndFoldersHandler;
    private final MailAddressHandler mailAddressHandler;
    private final CometService cometService;
    private final PersistenceHelper persistenceHelper;
    private final NewMessageHandler newMessageHandler;

	@Inject
    public MsgSynchroService(MessageDAO messageDAO,
							 TagDAO tagDAO,
							 ConnectionUtils connectionUtils,
							 PersistMessageHandler persistMessageHandler,
							 UpdateFlagsAndFoldersHandler updateFlagsAndFoldersHandler,
							 MailAddressHandler mailAddressHandler,
							 CometService cometService,
							 PersistenceHelper persistenceHelper,
							 NewMessageHandler newMessageHandler) {
		this.messageDAO = messageDAO;
		this.tagDAO = tagDAO;
		this.connectionUtils = connectionUtils;
		this.persistMessageHandler = persistMessageHandler;
		this.updateFlagsAndFoldersHandler = updateFlagsAndFoldersHandler;
		this.mailAddressHandler = mailAddressHandler;
		this.cometService = cometService;
		this.persistenceHelper = persistenceHelper;
		this.newMessageHandler = newMessageHandler;
	}

	public boolean synchronize(	final UserPO user,
								final MessageAccountPO account,
								final SyncUpdateMethod updateMode) {

		if(logger.isDebugEnabled())
			logger.debug("Starting synchronisation for account " + account.getName());
		long overallTime = System.currentTimeMillis();
        final Long accountId = account.getId();

        if(user == null || account == null) {
            throw new IllegalArgumentException();
        }
        String lockKey = user.getId().toString() + "_" + accountId.toString();
		ReentrantLock newLock = new ReentrantLock();
		ReentrantLock lock = lockMap.putIfAbsent(lockKey, newLock);
		if(lock == null) {
			lock = newLock; 
		}
		logger.info("Acquiring lock with key " + lockKey);
		lock.lock();
		logger.info("Lock " + lockKey + " acquired.");
		try {
			boolean newMessages = false;
			hashCache.clear();
			
			final Session session = connectionUtils.getSession(account, true);
			final Store store = connectionUtils.connectToStore(account, session);
			/* Handlers */
			final IncomingMessageHandler updateHandler;
            if(updateMode == SyncUpdateMethod.FLAGS)
                updateHandler = updateFlagsAndFoldersHandler;
            else if (updateMode == SyncUpdateMethod.FULL)
                updateHandler = persistMessageHandler;
            else if(updateMode == SyncUpdateMethod.NONE)
                updateHandler = null;
            else throw new IllegalArgumentException();

			IMAPFolder folder = null;
			try {
				Folder[] folders = store.getDefaultFolder().list("*");
				for (int j=0; j<folders.length; j++) {
					folder = (IMAPFolder)folders[j];
					if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
						if(logger.isInfoEnabled())
							logger.info("Synchronizing folder: ##### " + folder.getFullName() + " #####");						
						folder.open(Folder.READ_ONLY);
						long time = System.currentTimeMillis();
						/* Tag lineage */
						final TagLineagePO tagLineage = persistenceHelper.getOrCreateTagLineage(user, account, folder);
						
						final Map<Long, Message> uidMsgMap = getMessageUids(folder, updateMode);

						final Set<Long> remoteUids = uidMsgMap.keySet();
						final List<Long> knownUids = tagDAO.getAllMsgUidsForTagLineage(user.getId(), tagLineage.getId());

						/* Handle new Messages */
						@SuppressWarnings("unchecked")
						final List<Long> newUids = ListUtils.removeAll(remoteUids, knownUids);
						cometService.sendCometMessages(account.getUser().getId(), new AccountSyncProgressMessage(account.getUser().getId(), accountId, folder.getFullName(), newUids.size(), 0));
						if(newUids.size() > 0) {
                            // ignore failed messages
                            newMessages |= newMessageHandler.handleNewMessages(uidMsgMap, newUids, account, folder, tagLineage, updateHandler, persistMessageHandler, mailAddressHandler, session, user, hashCache);
						}
						
						/* Handle message updates if applicable */
						@SuppressWarnings("unchecked")
						final Collection<Long> updatedUids = CollectionUtils.intersection(remoteUids, knownUids);
						
						//handleMessageUpdates(uidMsgMap, updatedUids, account, folder, tagLineage, updateHandler, addressHandler, updateMode, session);
						final Map<Long, Boolean> dbReadFlagMap = isUpdateFlags(updateMode) ? messageDAO.getMsgUidFlagMapForTagLineage(user.getId(), tagLineage.getId()) : null;
						final Iterator<Long> it = updatedUids.iterator();
						long msgUpdateTime = 0;
						if(logger.isDebugEnabled())
							msgUpdateTime = System.currentTimeMillis();
						while(it.hasNext()) {
							final Long curMsgId = it.next();
							if(isUpdateFlags(updateMode)) {
								final boolean imapReadFlag = uidMsgMap.get(curMsgId).isSet(Flags.Flag.SEEN);
								// only update db message when read flags differ 
								if(imapReadFlag != dbReadFlagMap.get(curMsgId)) {
									if(logger.isDebugEnabled())
										logger.debug("Updating READ-Flag of existing message " + curMsgId);
                                    // TODO performance issue: this is VERY slow when updating lots of messages
                                    persistenceHelper.markMessageAsRead(user.getId(), curMsgId, tagLineage.getId(), imapReadFlag);
								}
							}
						}
						if(logger.isDebugEnabled())
							logger.debug("Message Update took: " + (System.currentTimeMillis() - msgUpdateTime) + "ms.");				
						
						/* Handle deleted messages (remove tag lineage from message) */
						@SuppressWarnings("unchecked")
						final Collection<Long> deleteUids = ListUtils.removeAll(knownUids, remoteUids); /* CollectionUtils.removeAll seems to be buggy (calls wrong ListUtils method) */
						if(!deleteUids.isEmpty())
                            handleDeletedMessages(deleteUids, user, tagLineage);

						// TODO recognize messages moved to another folder by another client (new message UID and tag lineage). Use unique business key (from/to/receivedDate)? Performance?
	
						if(logger.isInfoEnabled())
							logger.info("Folder synchronisation took " + (System.currentTimeMillis() - time) + "ms.");
						folder.close(false);
								
					}
				}

				final Date lastSyncRun = persistenceHelper.updateLastSyncRun(user, account.getId());

				/* Send Comet messages to clients */
				if(newMessages) {
					cometService.sendCometMessages(user.getId(), new NewMsgMessage/*LOL*/(user.getId(), accountId));
				}
				cometService.sendCometMessages(user.getId(), new AccountSyncedMessage(user.getId(), accountId, lastSyncRun));

				if(logger.isDebugEnabled())
					logger.debug("Overall sync duration: " + (System.currentTimeMillis() - overallTime) + "ms.");				
				return newMessages;
				
			} catch (AuthenticationFailedException e) {
				e.printStackTrace();
				throw new RuntimeException("Account synchronisation failed (Authentication failure): Username or Password wrong?");
			} catch (MessagingException e) {
				e.printStackTrace();
				throw new RuntimeException("Account synchronisation failed: " + e.getMessage());		
			} finally {
				if(folder != null && folder.isOpen()) {
					try {
						folder.close(false);
					} catch (MessagingException e) {
						e.printStackTrace();
						// Ignore
					}
				}
				if(store != null) {
					try {
						store.close();
					} catch (MessagingException e) {
						e.printStackTrace();
						// Ignore
					}
				}
			}
		} finally {
			logger.info("Releasing lock with key " + lockKey);
			lock.unlock();			
		}
	}

	private boolean isUpdateFlags(SyncUpdateMethod updateMode) {
		return SyncUpdateMethod.FLAGS.equals(updateMode) || SyncUpdateMethod.FULL.equals(updateMode);
	}

	private Map<Long, Message> getMessageUids(IMAPFolder folder, SyncUpdateMethod updateMode) throws MessagingException {
		Message[] messages;

		long time = System.currentTimeMillis();
		messages = folder.getMessages();
		if(logger.isDebugEnabled())
			logger.debug("IMAPFolder.getContacts() took " + (System.currentTimeMillis() - time) + "ms.");
	
		FetchProfile fp = new FetchProfile();
		fp.add(UIDFolder.FetchProfileItem.UID);
		if(isUpdateFlags(updateMode)) {
			logger.debug("Using Fetch Profile UID + FLAGS");
			fp.add(FetchProfileItem.FLAGS);
		}
		time = System.currentTimeMillis();
		folder.fetch(messages, fp);
		if(logger.isDebugEnabled())
			logger.debug("IMAPFolder.fetch() UIDs took " + (System.currentTimeMillis() - time) + "ms.");
	
		Map<Long, Message>uidMsgMap = new HashMap<Long, Message>();
		// TODO change everything to IMAP
		time = System.currentTimeMillis();
		for(int i=0; i<messages.length; i++)
			uidMsgMap.put(folder.getUID(messages[i]), messages[i]);
		if(logger.isDebugEnabled())
			logger.debug("Reading UIDs took " + (System.currentTimeMillis() - time) + "ms.");
		
		return uidMsgMap;
	}

    private void handleDeletedMessages(final Collection<Long> deleteUids, final UserPO userPO, final TagLineagePO tagLineage) {
        final int deleteCount = persistenceHelper.deleteMessagesForTagLineage(userPO.getId(), deleteUids, tagLineage.getId());
        if(logger.isDebugEnabled() && deleteCount > 0)
            logger.debug("Deleted " + deleteCount + " messages from database that where removed from IMAP folder before.");
    }
}
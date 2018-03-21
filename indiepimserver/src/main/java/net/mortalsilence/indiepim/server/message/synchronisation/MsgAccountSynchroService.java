package net.mortalsilence.indiepim.server.message.synchronisation;

import com.sun.mail.imap.IMAPFolder;
import net.mortalsilence.indiepim.server.pushmessage.PushMessageService;
import net.mortalsilence.indiepim.server.pushmessage.AccountSyncedMessage;
import net.mortalsilence.indiepim.server.pushmessage.NewMsgMessage;
import net.mortalsilence.indiepim.server.domain.*;
import net.mortalsilence.indiepim.server.message.ConnectionUtils;
import net.mortalsilence.indiepim.server.message.MessageConstants;
import net.mortalsilence.indiepim.server.message.SyncUpdateMethod;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.mail.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class MsgAccountSynchroService implements MessageConstants {

	private final static Logger logger = Logger.getLogger("net.mortalsilence.indiepim");
	private static final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();
	
	private static final Set<String> hashCache = new HashSet<>();
	private final ConnectionUtils connectionUtils;
	private final PersistenceHelper persistenceHelper;
	private final PushMessageService pushMessageService;
	private final IncomingMessageHandlerFactory incomingMessageHandlerFactory;
	private final FolderSynchroService folderSynchroService;

	@Inject
    public MsgAccountSynchroService(ConnectionUtils connectionUtils,
									PersistenceHelper persistenceHelper,
									PushMessageService pushMessageService,
									IncomingMessageHandlerFactory incomingMessageHandlerFactory,
									FolderSynchroService folderSynchroService) {
		this.connectionUtils = connectionUtils;
		this.persistenceHelper = persistenceHelper;
		this.pushMessageService = pushMessageService;
		this.incomingMessageHandlerFactory = incomingMessageHandlerFactory;
		this.folderSynchroService = folderSynchroService;
	}

	public void synchronize(UserPO user, MessageAccountPO account, SyncUpdateMethod updateMode) {

		logger.debug("Starting synchronisation for account " + account.getName());
		long overallTime = System.currentTimeMillis();
        final Long accountId = account.getId();

        String lockKey = user.getId().toString() + "_" + accountId;
		ReentrantLock lock = lockMap.computeIfAbsent(lockKey, s -> new ReentrantLock());
		logger.info("Acquiring lock with key " + lockKey);
		lock.lock();
		logger.info("Lock " + lockKey + " acquired.");
		try {
			hashCache.clear();

			final Session session = connectionUtils.getSession(account, true);
			/* Handlers */
			final IncomingMessageHandler updateHandler = incomingMessageHandlerFactory.getIncomingMessageHandler(updateMode);

			try (Store store = connectionUtils.connectToStore(account, session)) {
				boolean newMessages = false;
				for (Folder folder : store.getDefaultFolder().list("*")) {
					newMessages |= folderSynchroService.synchronizeFolder(account, updateMode, session, updateHandler, (IMAPFolder) folder, hashCache);
				}

				final Date lastSyncRun = persistenceHelper.updateLastSyncRun(user, account.getId());

				/* Send push messages to clients */
				if (newMessages) {
					pushMessageService.sendMessage(user.getId(), new NewMsgMessage/*LOL*/(user.getId(), accountId));
				}
				pushMessageService.sendMessage(user.getId(), new AccountSyncedMessage(user.getId(), accountId, lastSyncRun));

				logger.debug("Overall sync duration: " + (System.currentTimeMillis() - overallTime) + "ms.");
			} catch (AuthenticationFailedException e) {
				throw new RuntimeException("Account synchronisation failed (Authentication failure): Username or Password wrong?", e);
			} catch (MessagingException e) {
				throw new RuntimeException("Account synchronisation failed: " + e.getMessage(), e);
			}
		} finally {
			logger.info("Releasing lock with key " + lockKey);
			lock.unlock();			
		}
	}
}
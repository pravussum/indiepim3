package net.mortalsilence.indiepim.server.message.synchronisation

import com.sun.mail.imap.IMAPFolder
import net.mortalsilence.indiepim.server.PushMessageService
import net.mortalsilence.indiepim.server.dao.UserDAO
import net.mortalsilence.indiepim.server.domain.MessageAccountPO
import net.mortalsilence.indiepim.server.message.ConnectionUtils
import net.mortalsilence.indiepim.server.pushmessage.NewMsgMessage
import org.apache.log4j.Logger
import org.springframework.stereotype.Service
import javax.mail.Folder
import javax.mail.event.MessageCountAdapter
import javax.mail.event.MessageCountEvent
import javax.transaction.Transactional

@Service
class ImapFolderWatchingController(private val connectionUtils: ConnectionUtils,
                                   private val pushMessageService: PushMessageService,
                                   private val userDAO: UserDAO) {

    val logger = Logger.getLogger("net.mortalsilence.indiepim")

    private val user2WatchThreadMap = mutableMapOf<Long, Thread>()

    @Transactional
    fun watchAllAccountsForUser(userId : Long) {
        val user = userDAO.getUser(userId)
        if(user2WatchThreadMap.containsKey(user.id)) {
            return
        }
        user.messageAccounts.forEach {
            watchMessageAccount(it)
        }
    }

    private fun watchMessageAccount(account: MessageAccountPO) {
        val session = connectionUtils.getSession(account, true)
        val store = connectionUtils.connectToStore(account, session)
        val inbox = store.getFolder("INBOX")
        inbox.open(Folder.READ_WRITE)
        inbox.addMessageCountListener(object : MessageCountAdapter() {
            override fun messagesRemoved(e: MessageCountEvent?) {
                logger.info("Received message removed event")
                pushMessageService.sendMessage(account.user.id, NewMsgMessage(account.user.id, account.id))

            }

            override fun messagesAdded(e: MessageCountEvent?) {
                logger.info("Received message added event")
                pushMessageService.sendMessage(account.user.id, NewMsgMessage(account.user.id, account.id))
            }
        })
        val watcherThread = Thread(ImapFolderWatcher(inbox as IMAPFolder))
        user2WatchThreadMap[account.user.id] = watcherThread
        watcherThread.start()
    }
}
package net.mortalsilence.indiepim.server.message.synchronisation

import com.sun.mail.imap.IMAPFolder
import net.mortalsilence.indiepim.server.pushmessage.PushMessageService
import net.mortalsilence.indiepim.server.dao.MessageDAO
import net.mortalsilence.indiepim.server.dao.TagDAO
import net.mortalsilence.indiepim.server.domain.MessageAccountPO
import net.mortalsilence.indiepim.server.domain.TagLineagePO
import net.mortalsilence.indiepim.server.domain.UserPO
import net.mortalsilence.indiepim.server.message.SyncUpdateMethod
import net.mortalsilence.indiepim.server.pushmessage.AccountSyncProgressMessage
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.collections4.ListUtils
import org.apache.log4j.Logger
import org.springframework.stereotype.Component
import javax.mail.*

@Component
class FolderSynchroService
constructor(private val messageDAO: MessageDAO, private val tagDAO: TagDAO,
            private val newMessageHandler: NewMessageHandler,
            private val imapMessageFetcher: ImapMessageFetcher,
            private val persistenceHelper: PersistenceHelper,
            private val pushMessageService: PushMessageService) {

    @Throws(MessagingException::class)
    fun synchronizeFolder(account: MessageAccountPO, updateMode: SyncUpdateMethod,
                          session: Session, updateHandler: IncomingMessageHandler, folder: IMAPFolder, hashCache: Set<String>): Boolean {
        try {
            if (folder.type and Folder.HOLDS_MESSAGES == 0) {
                return false
            }

            logger.info("Synchronizing folder: ##### ${folder.fullName} #####")

            folder.open(Folder.READ_ONLY)
            val time = System.currentTimeMillis()
            /* Tag lineage */
            val tagLineage = persistenceHelper.getOrCreateTagLineage(account, folder.fullName, folder.separator)
            val remoteUid2MessageMap = imapMessageFetcher.getMessagesWithUid(folder, isUpdateFlags(updateMode))
            val (newUids, updatedUids, deleteUids) = getChangedUids(remoteUid2MessageMap, account, tagLineage)

            pushMessageService.sendMessage(account.user.id!!, AccountSyncProgressMessage(account.user.id!!, account.id!!, folder.fullName, newUids.size, 0))

            val newMessages = handleNewMessages(newUids, remoteUid2MessageMap, account, folder, tagLineage, session, hashCache, updateMode)
            handleUpdatedMessages(updateMode, account, tagLineage, updatedUids, remoteUid2MessageMap)
            handleDeletedMessages(deleteUids, account, tagLineage)

            // TODO recognize messages moved to another folder by another client (new message UID and tag lineage). Use unique business key (from/to/receivedDate)? Performance?

            logger.info("Folder synchronisation took " + (System.currentTimeMillis() - time) + "ms.")
            return newMessages
        } finally {
            folder.close(false)
        }
    }

    private fun handleNewMessages(newUids: MutableList<Long>, remoteUid2MessageMap: Map<Long, Message>?, account: MessageAccountPO, folder: IMAPFolder, tagLineage: TagLineagePO?, session: Session, hashCache: Set<String>, updateMode: SyncUpdateMethod): Boolean {
        if (newUids.size == 0) return false
        return newMessageHandler.handleNewMessages(remoteUid2MessageMap, newUids, account, folder, tagLineage, session, hashCache, updateMode)
    }

    private fun handleUpdatedMessages(updateMode: SyncUpdateMethod, account: MessageAccountPO, tagLineage: TagLineagePO, updatedUids: MutableCollection<Long>, remoteUid2MessageMap: MutableMap<Long, Message>) {
        if(!isUpdateFlags(updateMode)) return
        /* Handle message updates if applicable */
        val dbReadFlagMap = messageDAO.getMsgUidToReadFlagMap(account.user.id, tagLineage.id)
        val msgUpdateTime = System.currentTimeMillis()
        updatedUids.forEach { curMsgId ->
            val imapReadFlag = remoteUid2MessageMap[curMsgId]!!.isSet(Flags.Flag.SEEN)
            // only update db message when read flags differ
            if (imapReadFlag != dbReadFlagMap[curMsgId]) {
                logger.debug("Updating READ-Flag of existing message $curMsgId")
                // TODO performance issue: this is VERY slow when updating lots of messages
                persistenceHelper.markMessageAsRead(account.user.id, curMsgId, tagLineage.id, imapReadFlag)
            }
        }

        logger.debug("Message Update took: " + (System.currentTimeMillis() - msgUpdateTime) + "ms.")
    }

    private fun handleDeletedMessages(deleteUids: MutableList<Long>, account: MessageAccountPO, tagLineage: TagLineagePO) {
        if (!deleteUids.isEmpty())
            handleDeletedMessages(deleteUids, account.user, tagLineage)
    }

    private fun getChangedUids(uidMsgMap: MutableMap<Long, Message>,
                               account: MessageAccountPO,
                               tagLineage: TagLineagePO): Triple<MutableList<Long>, MutableCollection<Long>, MutableList<Long>> {
        val remoteUids = uidMsgMap.keys
        val knownUids = tagDAO.getAllMsgUidsForTagLineage(account.user.id, tagLineage.id)

        /* Handle new Messages */
        // TODO really slow
        val newUids = ListUtils.removeAll(remoteUids, knownUids)
        val updatedUids = CollectionUtils.intersection(remoteUids, knownUids)
        val deleteUids = ListUtils.removeAll(knownUids, remoteUids) /* CollectionUtils.removeAll seems to be buggy (calls wrong ListUtils method) */
        return Triple(newUids, updatedUids, deleteUids)
    }


    private fun isUpdateFlags(updateMode: SyncUpdateMethod): Boolean {
        return SyncUpdateMethod.FLAGS == updateMode || SyncUpdateMethod.FULL == updateMode
    }

    private fun handleDeletedMessages(deleteUids: Collection<Long>, userPO: UserPO, tagLineage: TagLineagePO) {
        val deleteCount = persistenceHelper.deleteMessagesForTagLineage(userPO.id, deleteUids, tagLineage.id)
        if (logger.isDebugEnabled && deleteCount > 0)
            logger.debug("Deleted $deleteCount messages from database that where removed from IMAP folder before.")
    }

    companion object {

        private val logger = Logger.getLogger("net.mortalsilence.indiepim")
    }
}

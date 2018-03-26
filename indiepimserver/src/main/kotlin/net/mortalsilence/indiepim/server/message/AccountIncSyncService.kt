package net.mortalsilence.indiepim.server.message

import net.mortalsilence.indiepim.server.domain.MessageAccountPO
import net.mortalsilence.indiepim.server.message.synchronisation.MsgAccountSynchroService
import org.apache.log4j.Logger
import org.springframework.stereotype.Component

@Component
class AccountIncSyncService(private val msgAccountSynchroService: MsgAccountSynchroService) {

    private val logger = Logger.getLogger("net.mortalsilence.indiepim")

    fun accountIncSync(account: MessageAccountPO) {
        val user = account.user
        try {
            if (account.messageAccountStats != null && account.messageAccountStats.lastSyncRun != null) {
                logger.info("Starting account synchronisation for user " + user.userName + ", account " + account.name + ".")
                msgAccountSynchroService.synchronize(user, account, account.syncMethod)
            } else {
                logger.info("Starting very first account synchronisation for user " + user.userName + ", account " + account.name)
                msgAccountSynchroService.synchronize(user, account, SyncUpdateMethod.NONE)
            }
        } catch (e: Exception) {
            logger.error("Account synchronisation failed", e)
        }

    }

}
package net.mortalsilence.indiepim.server.rest

import net.mortalsilence.indiepim.server.command.handler.ActionUtils
import net.mortalsilence.indiepim.server.dao.MessageDAO
import net.mortalsilence.indiepim.server.message.AccountIncSyncService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/account")
class AccountRestService(private val accountIncSyncService: AccountIncSyncService,
                         private val messageDAO: MessageDAO,
                         private val actionUtils: ActionUtils) {

    @GetMapping("/{accountId}/startIncSync")
    fun startIncSync(@PathVariable("accountId") accountId: Long) {
        val account = messageDAO.getMessageAccount(actionUtils.userId, accountId)

        // TODO make async, queue or disallow multiple requests
        accountIncSyncService.accountIncSync(account)
    }
}
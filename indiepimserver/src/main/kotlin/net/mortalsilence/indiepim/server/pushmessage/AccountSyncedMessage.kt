package net.mortalsilence.indiepim.server.pushmessage

import java.util.*

data class AccountSyncedMessage(val userId: Long,
                                val accountId: Long,
                                val syncDate: Date) : PushMessage {
    override fun getMessageType(): String {
        return "AccountSynced"
    }
}
package net.mortalsilence.indiepim.server.pushmessage

import java.util.*

data class AccountSyncedMessage(private val userId: Long,
                                private val accountId: Long,
                                private val syncDate: Date) : PushMessage {
    override fun getMessageType(): String {
        return "AccountSynced"
    }
}
package net.mortalsilence.indiepim.server.pushmessage

data class AccountSyncProgressMessage(private val userId: Long,
                                      private val accountId: Long,
                                      private val folder: String,
                                      private val msgCount: Int,
                                      private val msgDoneCount: Int) : PushMessage {

    override fun getMessageType(): String {
        return "AccountSyncProgress"
    }
}
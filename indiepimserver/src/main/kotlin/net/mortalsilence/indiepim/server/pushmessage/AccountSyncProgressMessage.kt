package net.mortalsilence.indiepim.server.pushmessage

data class AccountSyncProgressMessage(val userId: Long,
                                      val accountId: Long,
                                      val folder: String,
                                      val msgCount: Int,
                                      val msgDoneCount: Int) : PushMessage {

    override fun getMessageType(): String {
        return "AccountSyncProgress"
    }
}
package net.mortalsilence.indiepim.server.pushmessage

data class NewMsgMessage(private val userId: Long, private val accountId: Long) : PushMessage {
    override fun getMessageType(): String {
        return "NewMessage"
    }
}
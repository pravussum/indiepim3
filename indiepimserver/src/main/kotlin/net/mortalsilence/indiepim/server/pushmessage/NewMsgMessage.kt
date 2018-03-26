package net.mortalsilence.indiepim.server.pushmessage

data class NewMsgMessage(val userId: Long, val accountId: Long) : PushMessage {
    override fun getMessageType(): String {
        return "NewMessage"
    }
}
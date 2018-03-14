package net.mortalsilence.indiepim.server.pushmessage

data class NewChatMsgMessage (private val fromUserId: Long,
                              private val fromUserName: String,
                              private val message: String) : PushMessage {

    override fun getMessageType(): String {
        return "NewChatMessage"
    }
}
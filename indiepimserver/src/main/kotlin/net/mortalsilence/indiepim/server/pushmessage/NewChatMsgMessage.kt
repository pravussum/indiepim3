package net.mortalsilence.indiepim.server.pushmessage

data class NewChatMsgMessage (val fromUserId: Long,
                              val fromUserName: String,
                              val message: String) : PushMessage {

    override fun getMessageType(): String {
        return "NewChatMessage"
    }
}
package net.mortalsilence.indiepim.server.comet

data class NewChatMsgMessage (var fromUserId: Long? = null,
                              var fromUserName: String? = null,
                              var message: String? = null) : PushMessage {

    override fun getMessageType(): String {
        return "NewChatMessage"
    }
}
package net.mortalsilence.indiepim.server.pushmessage

data class BackendErrorPushMessage(private val errorMessage: String): PushMessage {

    override fun getMessageType(): String {
        return "BackendError";
    }
}
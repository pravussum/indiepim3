package net.mortalsilence.indiepim.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.HashMultimap
import com.google.common.collect.SetMultimap
import net.mortalsilence.indiepim.server.pushmessage.PushMessage
import org.apache.log4j.Logger
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

@Service
class PushMessageService(val objectMapper: ObjectMapper) {
    val logger: Logger = Logger.getLogger("net.mortalsilence.indiepim")
    val userId2SessionMap : SetMultimap<Long, WebSocketSession> = HashMultimap.create()

    fun addSession(userId: Long, session: WebSocketSession) {
        userId2SessionMap.get(userId).add(session)
    }

    fun removeSession(userId: Long, session: WebSocketSession) {
        userId2SessionMap.remove(userId, session)
    }

    fun sendMessage(userId: Long, message: PushMessage) {
        val json = objectMapper.writeValueAsString(message)
        userId2SessionMap.get(userId).forEach {session -> session.sendMessage(TextMessage(json)) }
    }

    fun sendBroadcast(message: PushMessage, excludedUserId: Long?) {

        userId2SessionMap.entries().forEach { entry ->
            if (entry.key != excludedUserId) sendMessage(entry.key, message)
        }
    }

    fun getOnlineUsers(userId: Long) : Set<Long> {
        return userId2SessionMap.keySet().filter { it != userId }.toSet()
    }
}
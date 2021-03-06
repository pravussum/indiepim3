package net.mortalsilence.indiepim.server.spring

import net.mortalsilence.indiepim.server.message.synchronisation.ImapFolderWatchingController
import net.mortalsilence.indiepim.server.pushmessage.PushMessageService
import net.mortalsilence.indiepim.server.security.IndieUser
import org.apache.log4j.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.handler.TextWebSocketHandler

@EnableWebSocket
@Configuration
class WebSocketConfiguration(val pushMessageService: PushMessageService,
                             val imapFolderWatchingController: ImapFolderWatchingController) {

    private val logger = Logger.getLogger("net.mortalsilence.indiepim")

    @Bean
    fun webSocketConfigurer(webSocketHandler: WebSocketHandler) : WebSocketConfigurer {
        return WebSocketConfigurer {registry ->
            registry.addHandler(webSocketHandler, "/ws/messages")
        }
    }

    @Bean
    fun wsh() : WebSocketHandler {
        return object : TextWebSocketHandler() {
            override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
                super.handleTextMessage(session, message)
                logger.debug("handling text message $message")
            }

            override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
                val indieUser : IndieUser = (session.principal as UsernamePasswordAuthenticationToken).principal as IndieUser
                pushMessageService.removeSession(indieUser.id, session)
                super.afterConnectionClosed(session, status)
            }

            override fun afterConnectionEstablished(session: WebSocketSession) {
                val indieUser : IndieUser = (session.principal as UsernamePasswordAuthenticationToken).principal as IndieUser
                pushMessageService.addSession(indieUser.id, session)
                imapFolderWatchingController.watchAllAccountsForUser(indieUser.id)
                logger.info("connections established for user ${session.principal?.name}")
            }
        }
    }
}
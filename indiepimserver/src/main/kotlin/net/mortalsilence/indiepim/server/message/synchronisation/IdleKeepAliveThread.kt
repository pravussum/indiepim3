package net.mortalsilence.indiepim.server.message.synchronisation

import com.sun.mail.imap.IMAPFolder
import org.apache.log4j.Logger
import javax.mail.MessagingException

const val KEEP_ALIVE_FREQ = 300000L

class IdleKeepAliveThread(private val imapFolder : IMAPFolder) : Runnable {

    val logger = Logger.getLogger("net.mortalsilence.indiepim")

    override fun run() {
        logger.info("Keepalive thread for folder ${imapFolder.name} running...")
        while(!Thread.interrupted()) {
            try {
                Thread.sleep(KEEP_ALIVE_FREQ)
                imapFolder.doCommand({ protocol -> protocol.simpleCommand("NOOP", null) })
            } catch(ignore : InterruptedException) {
            } catch(e : MessagingException) {
                logger.warn(e)
            }
        }
        logger.info("Keepalive thread for folder ${imapFolder.name} exiting...")
    }

}
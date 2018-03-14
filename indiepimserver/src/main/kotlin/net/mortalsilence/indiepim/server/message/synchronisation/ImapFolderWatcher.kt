package net.mortalsilence.indiepim.server.message.synchronisation

import com.sun.mail.imap.IMAPFolder
import org.apache.log4j.Logger
import javax.mail.MessagingException

class ImapFolderWatcher(private val imapFolder: IMAPFolder) : Runnable{

    val logger = Logger.getLogger("net.mortalsilence.indiepim")

    override fun run() {
        logger.info("ImapFolderWatcher for folder ${imapFolder.name} starting...")
        val keepAliveThread = Thread(IdleKeepAliveThread(imapFolder), "IdleKeepAlive${imapFolder.name}")
        keepAliveThread.start()
        while(!Thread.interrupted()) {
            try {
                imapFolder.idle()
            } catch (e: MessagingException) {
                logger.warn("MessagingException during IDLE call for folder ${imapFolder.name}- supported by server?")
                throw RuntimeException(e)
            }
        }
        if(keepAliveThread.isAlive) {
            keepAliveThread.interrupt()
        }
        logger.info("ImapFolderWatcher for folder ${imapFolder.name} exiting...")
    }

}
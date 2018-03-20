package net.mortalsilence.indiepim.server.message.synchronisation

import net.mortalsilence.indiepim.server.dao.GenericDAO
import net.mortalsilence.indiepim.server.dao.MessageDAO
import net.mortalsilence.indiepim.server.domain.*
import net.mortalsilence.indiepim.server.message.MessageConstants
import net.mortalsilence.indiepim.server.utils.MessageUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.log4j.Logger
import org.springframework.stereotype.Service
import java.io.*
import java.nio.charset.CodingErrorAction
import javax.mail.*
import javax.mail.internet.ContentType
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeUtility

val logger = Logger.getLogger("net.mortalsilence.indiepim")

@Service
class PersistMessageHandler(private val genericDAO: GenericDAO,
                            private val messageDAO: MessageDAO,
                            private val messageUtils: MessageUtils,
                            private val mimeToJavaCharsetMapper: MimeToJavaCharsetMapper) : IncomingMessageHandler, MessageConstants {

    init {
        // TODO: rework
        System.getProperties()["mail.mime.base64.ignoreerrors"] = "true"
        System.getProperties()["mail.mime.ignoreunknownencoding"] = "true"
        System.getProperties()["mail.mime.decodetext.strict"] = "false"
        System.getProperties()["mail.mime.multipart.allowempty"] = "true"
        System.getProperties()["mail.mime.parameters.strict"] = "false"
    }

    override fun handleMessage(message: Message,
                               msg: MessagePO,
                               msgUid: Long,
                               account: MessageAccountPO,
                               folder: Folder,
                               tagLineage: TagLineagePO,
                               session: Session, user: UserPO): MessagePO {
        var result: MessagePO
        msg.user = user
        msg.messageAccount = account
        try {
            try {
                msg.subject = if (message.subject != null) MimeUtility.decodeText(message.subject) else null
            } catch (e: UnsupportedEncodingException) {
                logger.debug("UnsupportEncodingExeption", e)
            }

            msg.size = message.size
            msg.dateReceived = message.receivedDate

            /* Get the SEEN flag before the content is received, because the IMAP server will then set the SEEN flag */
            handleFlags(message, msg)

            handleContent(message, msgUid, msg, session)

            // TODO re-unset the SEEN flag if it was not set? SEEN flag is then updated when first shown via web client... For now working probably due to READONLY mode

            handleAddresses(message, msg)

            /* PERSIST */
            result = genericDAO.updateOrPersist(msg)

            messageDAO.addTagLineage(msg, tagLineage, msgUid)

        } catch (e: MessagingException) {
            /* Workaround for email server bugs */
            logger.error("Error occured parsing message " + msgUid, e)
            if (e.message == "Unable to load BODYSTRUCTURE" && message is MimeMessage) {
                try {
                    val copy = MimeMessage(message)
                    result = handleMessage(copy, msg, msgUid, account, folder, tagLineage, session, user)
                } catch (e1: MessagingException) {
                    e.printStackTrace()
                    throw RuntimeException(e)
                }
            } else {
                e.printStackTrace()
                throw RuntimeException(e)
            }
        }

        return result
    }

    @Throws(MessagingException::class)
    private fun handleFlags(message: Message, msg: MessagePO) {
        msg.read = message.isSet(Flags.Flag.SEEN)
    }

    @Throws(MessagingException::class)
    private fun handleAddresses(message: Message, msg: MessagePO) {
        /* handle recipients */
        msg.receiver = messageUtils.getReceiverStr(message)

        /* handle sender(s) */
        val senderStr = messageUtils.getSenderStr(message)
        msg.sender = senderStr
    }

    @Throws(MessagingException::class)
    private fun handleContent(message: Message, msgUid: Long, msg: MessagePO, session: Session) {
        /* remove all former attachments (for message updates) */
        msg.attachments.clear()
        val rebuiltMessage = rebuildFromSourceIfNecessary(message, msgUid, session)

        /* recursivly iterate through the message parts */
        handleMessagePartAlternative(msg, msgUid, 1, rebuiltMessage)
    }

    @Throws(MessagingException::class)
    private fun rebuildFromSourceIfNecessary(message: Message, msgUid: Long, session: Session): Message {
        /* This is to be able to parse invalid MIME messages, that get recognized as text/plain
		 * by an overstrict IMAP server like Courier */
        try {
            val content = message.content as String
            if (message.contentType.equals(MessageConstants.CONTENT_TYPE_TEXT_PLAIN, ignoreCase = true) && content.indexOf(MessageConstants.CONTENT_TYPE) > 0) {
                logger.info("Rebuilding message $msgUid from original message source.")
                val baos = ByteArrayOutputStream()
                message.writeTo(baos)
                return MimeMessage(session, ByteArrayInputStream(baos.toByteArray()))
            }
        } catch (ioe: IOException) {
            /* never mind, was worth a try */
            logger.error("Error parsing message content for message uid: $msgUid", ioe)
        } catch (cce: ClassCastException) {
            /* Not a text/plain message -> ignore */
        }

        return message
    }

    @Throws(MessagingException::class)
    private fun handleMessagePartAlternative(msg: MessagePO, msgUid: Long?, partPosNo: Int, part: Part): Int {
        var localPartPosNo = partPosNo
        part.contentType ?: throw RuntimeException("No Content-Type")
        /*
         * PLAIN TEXT OR HTML and text/html not already set by former message part --> save as attachment in this case
         */
        if (part.isMimeType(MessageConstants.CONTENT_TYPE_TEXT_HTML) && msg.contentHtml == null || part.isMimeType(MessageConstants.CONTENT_TYPE_TEXT_PLAIN) && msg.contentText == null) {
            try {
                val `is` = part.inputStream
                val mimeCharset = ContentType(part.contentType).getParameter(MessageConstants.CONTENT_TYPE_PARAM_CHARSET)
                val charset = mimeToJavaCharsetMapper.getJavaCharsetFromMimeCharsetWithFallback(msgUid, mimeCharset)
                // drop malformed Characters
                val decoder = charset.newDecoder()
                decoder.onMalformedInput(CodingErrorAction.IGNORE)
                decoder.onUnmappableCharacter(CodingErrorAction.IGNORE)
                val inputReader = InputStreamReader(`is`, decoder)
                if (part.isMimeType(MessageConstants.CONTENT_TYPE_TEXT_PLAIN)) {
                    msg.contentText = IOUtils.toString(inputReader)
                } else if (part.isMimeType(MessageConstants.CONTENT_TYPE_TEXT_HTML)) {
                    msg.contentHtml = IOUtils.toString(inputReader)
                }
            } catch (ioe: IOException) {
                throw RuntimeException(ioe)
            }

            localPartPosNo++
            /*
         * MULTIPART --> recursivly go through the parts
         */
        } else if (part.isMimeType(MessageConstants.CONTENT_TYPE_MULTIPART_ALL)) {
            try {
                val multipart = part.content as Multipart
                for (i in 0 until multipart.count) {
                    localPartPosNo = handleMessagePartAlternative(msg, msgUid, localPartPosNo, multipart.getBodyPart(i))
                }
            } catch (ioe: IOException) {
                throw RuntimeException(ioe)
            }

            /*
         * everything else --> handle es attachment
         */
        } else {
            val attachment = createAttachment(msg, part)
            attachment.posNo = localPartPosNo
            localPartPosNo++
            // multipart --> recursive
            // part --> unwrap
            // images or other text or anything --> attachment
        }
        return localPartPosNo
    }

    @Throws(MessagingException::class)
    private fun createAttachment(msg: MessagePO, part: Part): AttachmentPO {
        val attachment = AttachmentPO()
        attachment.message = msg
        attachment.user = msg.user
        attachment.filename = StringUtils.abbreviate(part.fileName, 300)
        attachment.mimeType = StringUtils.abbreviate(part.contentType, 150)
        attachment.disposition = part.disposition
        return attachment
    }
}

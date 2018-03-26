package net.mortalsilence.indiepim.server.message.synchronisation

import org.springframework.stereotype.Component
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import javax.mail.internet.MimeUtility

@Component
class MimeToJavaCharsetMapper {

    fun getJavaCharsetFromMimeCharsetWithFallback(msgUid: Long?, mimeCharset: String?): Charset {
        if(mimeCharset == null) {
            return Charset.defaultCharset()
        }
        val javaCharset: String? = MimeUtility.javaCharset(mimeCharset)
        if (javaCharset == null) {
            logger.warn("No java charset for given MIME charset $mimeCharset (message $msgUid. Using default charset.")
            return  Charset.defaultCharset()
        }
        try {
            return Charset.forName(javaCharset)
        } catch (e: UnsupportedCharsetException) {
            logger.warn("No java charset for given MIME charset $mimeCharset (message $msgUid. Using default charset.")
            return Charset.defaultCharset()
        }

    }

}

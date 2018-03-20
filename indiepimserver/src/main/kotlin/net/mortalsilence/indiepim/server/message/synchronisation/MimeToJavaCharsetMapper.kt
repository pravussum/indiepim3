package net.mortalsilence.indiepim.server.message.synchronisation

import org.springframework.stereotype.Component
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import javax.mail.internet.MimeUtility

@Component
class MimeToJavaCharsetMapper {

    fun getJavaCharsetFromMimeCharsetWithFallback(msgUid: Long?, mimeCharset: String): Charset {
        var javaCharset: String? = MimeUtility.javaCharset(mimeCharset)
        if (javaCharset == null) {
            javaCharset = Charset.defaultCharset().name()
            logger.warn("No java charset for given MIME charset $mimeCharset (message $msgUid. Using default charset.")
        }
        var charset: Charset
        try {
            charset = Charset.forName(javaCharset!!)
        } catch (e: UnsupportedCharsetException) {
            charset = Charset.defaultCharset()
            logger.warn("No java charset for given MIME charset $mimeCharset (message $msgUid. Using default charset.")
        }

        return charset
    }

}

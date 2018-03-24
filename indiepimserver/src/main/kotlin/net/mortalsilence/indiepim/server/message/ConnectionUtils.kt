package net.mortalsilence.indiepim.server.message

import net.mortalsilence.indiepim.server.SharedConstants
import net.mortalsilence.indiepim.server.dao.GenericDAO
import net.mortalsilence.indiepim.server.dao.TagDAO
import net.mortalsilence.indiepim.server.domain.MessageAccountPO
import net.mortalsilence.indiepim.server.domain.TagHierarchyPO
import net.mortalsilence.indiepim.server.domain.TagLineagePO
import net.mortalsilence.indiepim.server.exception.NotImplementedException
import net.mortalsilence.indiepim.server.security.EncryptionService
import net.mortalsilence.indiepim.server.utils.ArgUtils
import org.apache.commons.lang3.StringUtils
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Store
import javax.mail.Transport

/*

	Protocol	Store or	Uses	Supports
	Name		Transport?	SSL?	STARTTLS?
	-------------------------------------------------
	imap		Store		No	    Yes
	imaps		Store		Yes	    N/A
	gimap		Store		Yes	    N/A
	pop3		Store		No	    Yes
	pop3s		Store		Yes	    N/A
	smtp		Transport	No	    Yes
	smtps		Transport	Yes	    N/A
 */

@Named
class ConnectionUtils @Inject
constructor(private val tagDAO: TagDAO, private val genericDAO: GenericDAO,
            private val encryptionService: EncryptionService) : MessageConstants {

    private val debug = false

    fun connectToStore(account: MessageAccountPO, session: Session): Store {
        val password = encryptionService.decypher(account.password)
        val store: Store
        try {
            store = session.getStore(getIncomingProtocol(account))
            store.connect(account.host, account.port!!, account.username, password)
            return store
        } catch (e: MessagingException) {
            throw RuntimeException("Connecting to message store failed for account $account.id ($account.host:$account.port)", e)
        }
    }

    fun getTransport(account: MessageAccountPO, session: Session, protocol: String): Transport {

        val password = encryptionService.decypher(account.password)
        val port = account.outgoingPort
        val transport: Transport
        try {
            transport = session.getTransport(protocol)
            transport.connect(account.outgoingHost, port!!, account.email, password)
            return transport
        } catch (e: MessagingException) {
            throw RuntimeException("Connection failed for account $account.id ($account.outgoingHost:$port)", e)
        }
    }

    fun getSession(account: MessageAccountPO, incoming: Boolean): Session {
        // Get system properties
        val props = System.getProperties()
        /* Protocol */
        props.setProperty("mail.store.protocol", getOutgoingProtocol(account))
        val protocol = if (incoming) getIncomingProtocol(account) else getOutgoingProtocol(account)

        props.remove("mail.$protocol.host")
        props.remove("mail.$protocol.port")
        props.remove("mail.$protocol.user")
        props.remove("mail.user")

        /* address parsing */
        props.setProperty("mail.mime.address.strict", "false")

        /* Authentication */
        val authenticationMode = if (incoming) account.authentication else account.outgoingAuthentication
        val auth = if (authenticationMode == AuthenticationMode.PASSWORD_NORMAL) "true" else "false"
        props["mail.$protocol.auth"] = auth

        /* Encryption */
        val encryptionMode = if (incoming) account.encryption else account.outgoingEncryption
        val starttls = if (encryptionMode == EncryptionMode.STARTTLS) "true" else "false"
        props["mail.$protocol.starttls.enable"] = starttls

        props["mail.debug"] = if (debug) "true" else "false"
        if (account.trustInvalidSSLCertificates) {
            /*
            SELF SIGNED CERTIFICATES

            JavaMail now includes a special SSL socket factory that can simplify
            dealing with servers with self-signed certificates.  While the
            recommended approach is to include the certificate in your keystore
            as described above, the following approach may be simpler in some cases.

            The class com.sun.mail.util.MailSSLSocketFactory can be used as a
            simple socket factory that allows trusting all hosts or a specific set
            of hosts.  For example:

            ...

            Use of MailSSLSocketFactory avoids the need to add the certificate to
            your keystore as described above, or configure your own TrustManager
            as described below.
            ---
            mail.smtp.ssl.trust
            If set, and a socket factory hasn't been specified, enables use of a
            MailSSLSocketFactory. If set to "*", all hosts are trusted. If set to a
            whitespace separated list of hosts, those hosts are trusted. Otherwise, trust
            depends on the certificate the server presents.

            */
            props["mail.$protocol.ssl.trust"] = if (incoming) account.host else account.outgoingHost

            /*  -- Server Identity Check --
                RFC 2595 specifies addition checks that must be performed on the
                server's certificate to ensure that the server you connected to is
                the server you intended to connect to.  This reduces the risk of
                "man in the middle" attacks.  For compatibility with earlier releases
                of JavaMail, these additional checks are disabled by default.  We
                    strongly recommend that you enable these checks when using SSL.  To
                    enable these checks, set the "mail..ssl.checkserveridentity"
                    property to "true".
            */
            props["mail.$protocol.ssl.checkserveridentity"] = "false" // accept self-signed certificate ?


            // TODO [Security] ask user during account setup and import certificate to JDK trust store
            // keytool -import -file your_cert.cer /path/to/cacerts, the password is changeit
        }

        return Session.getInstance(props)
    }

    fun getIncomingProtocol(account: MessageAccountPO): String {
        if (ArgUtils.empty(account.protocol))
            throw RuntimeException("Protocol for account " + account.name + " is not set.")
        if (MessageConstants.PROTOCOL_IMAP == account.protocol.toUpperCase()) {
            return if (account.encryption == EncryptionMode.SSL) "imaps" else "imap"
        } else if (MessageConstants.PROTOCOL_POP3 == account.protocol.toUpperCase()) {
            return if (account.encryption == EncryptionMode.SSL) "imaps" else "imap"
        }
        throw NotImplementedException("Protocol " + account.protocol + " not (yet) supported. Valid values are " + MessageConstants.PROTOCOL_IMAP + " and " + MessageConstants.PROTOCOL_POP3)
    }

    fun getOutgoingProtocol(account: MessageAccountPO): String {
        return if (account.encryption == EncryptionMode.SSL) "smtps" else "smtp"
    }

    fun getFolderPathFromTagLineage(separator: Char, tagLineage: TagLineagePO): String? {
        val parts = StringUtils.split(tagLineage.lineage, SharedConstants.TAG_LINEAGE_SEPARATOR.toString())
        return StringUtils.join(parts, separator)
    }

    @Throws(MessagingException::class)
    fun getSentFolderPath(account: MessageAccountPO, store: Store): String? {
        val separator = store.defaultFolder.separator
        return if (account.sentFolder == null) getDefaultFolderName(store) + separator + MessageConstants.DEFAULT_FOLDER_SENT else getFolderPathFromTagLineage(separator, account.sentFolder)
    }

    @Throws(MessagingException::class)
    fun getTrashFolderPath(account: MessageAccountPO, store: Store): String? {
        val separator = store.defaultFolder.separator
        return if (account.trashFolder == null) getDefaultFolderName(store) + separator + MessageConstants.DEFAULT_FOLDER_TRASH else getFolderPathFromTagLineage(separator, account.trashFolder)
    }

    @Throws(MessagingException::class)
    fun getJunkFolderPath(account: MessageAccountPO, store: Store): String? {
        val separator = store.defaultFolder.separator
        return if (account.junkFolder == null) getDefaultFolderName(store) + separator + MessageConstants.DEFAULT_FOLDER_JUNK else getFolderPathFromTagLineage(separator, account.junkFolder)
    }

    @Throws(MessagingException::class)
    fun getDraftsFolderPath(account: MessageAccountPO, store: Store): String? {
        val separator = store.defaultFolder.separator
        return if (account.draftsFolder == null) getDefaultFolderName(store) + separator + MessageConstants.DEFAULT_FOLDER_DRAFTS else getFolderPathFromTagLineage(separator, account.draftsFolder)
    }

    @Throws(MessagingException::class)
    private fun getDefaultFolderName(store: Store): String {
        //         return store.getDefaultFolder().getName();
        // TODO store.getDefaultFolder() seems to return empty strings, at least sometimes... what to do about it?
        return MessageConstants.DEFAULT_FOLDER_INBOX
    }


    fun getOrCreateTagLineage(account: MessageAccountPO, folderFullName: String, folderSeparator: Char): TagLineagePO? {

        val tags = LinkedList<String>()
        val st = StringTokenizer(folderFullName, Character.toString(folderSeparator))
        while (st.hasMoreElements()) {
            tags.add(st.nextToken())
        }
        if (account.tagHierarchy == null) {
            createTagHierarchy4Account(account)
        }

        val partialTagLineageTags = LinkedList<String>()
        val tagIt = tags.iterator()
        var tagLineage: TagLineagePO? = null
        while (tagIt.hasNext()) {
            partialTagLineageTags.add(tagIt.next())
            val tagLineageStr = StringUtils.join(partialTagLineageTags, SharedConstants.TAG_LINEAGE_SEPARATOR)
            tagLineage = tagDAO.getOrCreateTagLineage(account.user, tagLineageStr, account.tagHierarchy)
        }

        return tagLineage
    }

    fun createTagHierarchy4Account(account: MessageAccountPO) {
        val tagHierarchy = TagHierarchyPO()
        tagHierarchy.user = account.user
        tagHierarchy.name = account.tag.tag
        genericDAO.persist(tagHierarchy)
        account.tagHierarchy = tagHierarchy
        genericDAO.merge(account)
    }
}

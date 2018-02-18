package net.mortalsilence.indiepim.server.domain;

import net.mortalsilence.indiepim.server.message.AuthenticationMode;
import net.mortalsilence.indiepim.server.message.EncryptionMode;
import net.mortalsilence.indiepim.server.message.MessageDeleteMethod;
import net.mortalsilence.indiepim.server.message.SyncUpdateMethod;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "msg_account")
public class MessageAccountPO implements Serializable, PersistentObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Version
	@Column(name = "ts_update")
	private Timestamp tsUpdate;
	
	@Column(name="email")
	private String email;
	
	@Column(name = "host")
	private String host;
	
	@Column(name = "username")
	private String username;
	
	
	@Column(name = "password")
	private String password;

	@Column(name = "outgoing_host")
	private String outgoingHost;

	@Column(name = "port")
	private Integer port;
	
	@Column(name = "outgoing_port")
	private Integer outgoingPort;
	
	@Column(name = "authentication")
	@Enumerated(EnumType.STRING)
	private AuthenticationMode authentication;
	
	@Column(name = "outgoing_authentication")
	@Enumerated(EnumType.STRING)
	private AuthenticationMode outgoingAuthentication;
	
	@Column(name = "encryption")
	@Enumerated(EnumType.STRING)
	private EncryptionMode encryption;
	
	@Column(name = "outgoing_encryption")
	@Enumerated(EnumType.STRING)
	private EncryptionMode outgoingEncryption;
	
	@Column(name="name")
	private String name;
	
	@ManyToOne(optional=false)
	@JoinColumn(name="user_id", referencedColumnName="id")
	private UserPO user;

	@Column(name = "protocol")
	private String protocol;
	
	@OneToOne
	@JoinColumn(name = "tag_hierarchy_id")
	@Cascade( {CascadeType.DELETE})
	private TagHierarchyPO tagHierarchy;
	
	@OneToOne(optional=false)
	@JoinColumn(name="tag_id", referencedColumnName="id")
	private TagPO tag;

	@OneToMany( mappedBy="messageAccount", targetEntity=MessagePO.class)
	@Cascade( {CascadeType.DELETE})
	private List<MessagePO> messages = new LinkedList<>();
	
	/*
	 * sync method (new msg. always persisted, deleted always removed):
	 * 1	update read flag + update folder info
	 * 2	update entire message
	 */
	@Column(name = "syncMethod")
	@Enumerated(EnumType.STRING)
	private SyncUpdateMethod syncMethod;
	
	/*
	 * The synchronisation interval
	 */

	@Column(name = "syncInterval")
	private Integer syncInterval;

	@Column(name = "newMessages")
	private Boolean newMessages;
	
	@Column(name = "trustInvalidSSLCertificates")
	private boolean trustInvalidSSLCertificates = false;

    @OneToOne (optional = false)
    @JoinColumn(name = "msg_account_stats_id", referencedColumnName = "id")
    private MessageAccountStatsPO messageAccountStats;

    @ManyToOne
    @JoinColumn(name="sent_tag_lineage_id", referencedColumnName="id")
    private TagLineagePO sentFolder;

    @ManyToOne
    @JoinColumn(name="trash_tag_lineage_id", referencedColumnName="id")
    private TagLineagePO trashFolder;

    @ManyToOne
    @JoinColumn(name="junk_tag_lineage_id", referencedColumnName="id")
    private TagLineagePO junkFolder;

    @ManyToOne
    @JoinColumn(name="drafts_tag_lineage_id", referencedColumnName="id")
    private TagLineagePO draftsFolder;

    @Column (name = "delete_mode")
    @Enumerated(EnumType.STRING)
    private MessageDeleteMethod deleteMode;

    public SyncUpdateMethod getSyncMethod() {
		return syncMethod;
	}

	public void setSyncMethod(SyncUpdateMethod syncMethod) {
		this.syncMethod = syncMethod;
	}

	public Integer getSyncInterval() {
		return syncInterval;
	}

	public void setSyncInterval(Integer syncInterval) {
		this.syncInterval = syncInterval;
	}

	public Boolean getNewMessages() {
		return newMessages;
	}

	public void setNewMessages(Boolean newMessages) {
		this.newMessages = newMessages;
	}

	public boolean getTrustInvalidSSLCertificates() {
		return trustInvalidSSLCertificates;
	}

	public void setTrustInvalidSSLCertificates(boolean trustInvalidSSLCertificates) {
		this.trustInvalidSSLCertificates = trustInvalidSSLCertificates;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UserPO getUser() {
		return user;
	}

	public void setUser(UserPO user) {
		this.user = user;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public TagHierarchyPO getTagHierarchy() {
		return tagHierarchy;
	}

	public void setTagHierarchy(TagHierarchyPO tagHiearchy) {
		this.tagHierarchy = tagHiearchy;
	}

	public TagPO getTag() {
		return tag;
	}

	public void setTag(TagPO tag) {
		this.tag = tag;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getOutgoingHost() {
		return outgoingHost;
	}

	public void setOutgoingHost(String outgoingHost) {
		this.outgoingHost = outgoingHost;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getOutgoingPort() {
		return outgoingPort;
	}

	public void setOutgoingPort(Integer outgoingPort) {
		this.outgoingPort = outgoingPort;
	}

	public AuthenticationMode getAuthentication() {
		return authentication;
	}

	public void setAuthentication(AuthenticationMode authentication) {
		this.authentication = authentication;
	}

	public AuthenticationMode getOutgoingAuthentication() {
		return outgoingAuthentication;
	}

	public void setOutgoingAuthentication(AuthenticationMode outgoingAuthentication) {
		this.outgoingAuthentication = outgoingAuthentication;
	}

	public EncryptionMode getEncryption() {
		return encryption;
	}

	public void setEncryption(EncryptionMode encryption) {
		this.encryption = encryption;
	}

	public EncryptionMode getOutgoingEncryption() {
		return outgoingEncryption;
	}

	public void setOutgoingEncryption(EncryptionMode outgoingEncryption) {
		this.outgoingEncryption = outgoingEncryption;
	}

	/*
	 * This can be insanely slow!! Don't use it!
	 */
	@Deprecated 
	public List<MessagePO> getMessages() {
		return messages;
	}

	public void setMessages(List<MessagePO> messages) {
		this.messages = messages;
	}

	public Timestamp getTsUpdate() {
		return tsUpdate;
	}

	public void setTsUpdate(Timestamp tsUpdate) {
		this.tsUpdate = tsUpdate;
	}

    public MessageAccountStatsPO getMessageAccountStats() {
        return messageAccountStats;
    }

    public void setMessageAccountStats(MessageAccountStatsPO messageAccountStats) {
        this.messageAccountStats = messageAccountStats;
    }

    public TagLineagePO getSentFolder() {
        return sentFolder;
    }

    public void setSentFolder(TagLineagePO sentFolder) {
        this.sentFolder = sentFolder;
    }

    public TagLineagePO getTrashFolder() {
        return trashFolder;
    }

    public void setTrashFolder(TagLineagePO trashFolder) {
        this.trashFolder = trashFolder;
    }

    public TagLineagePO getJunkFolder() {
        return junkFolder;
    }

    public void setJunkFolder(TagLineagePO junkFolder) {
        this.junkFolder = junkFolder;
    }

    public TagLineagePO getDraftsFolder() {
        return draftsFolder;
    }

    public void setDraftsFolder(TagLineagePO draftsFolder) {
        this.draftsFolder = draftsFolder;
    }

    public MessageDeleteMethod getDeleteMode() {
        return deleteMode;
    }

    public void setDeleteMode(MessageDeleteMethod deleteMode) {
        this.deleteMode = deleteMode;
    }
}

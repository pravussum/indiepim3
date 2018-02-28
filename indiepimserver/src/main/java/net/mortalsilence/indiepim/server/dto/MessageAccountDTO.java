package net.mortalsilence.indiepim.server.dto;

import net.mortalsilence.indiepim.server.message.AuthenticationMode;
import net.mortalsilence.indiepim.server.message.EncryptionMode;
import net.mortalsilence.indiepim.server.message.SyncUpdateMethod;
import net.mortalsilence.indiepim.server.tags.TagHierarchyNode;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

public class MessageAccountDTO implements Serializable {

	public Long id;
	public String email;
	public String accountName;
	public String userName;
	public String host;
	public String outgoingHost;
	public Integer port;
	public Integer outgoingPort;
	public AuthenticationMode authentication;
	public AuthenticationMode outgoingAuthentication;
	public EncryptionMode encryption;
	public EncryptionMode outgoingEncryption;
	public String tag;
	public String password;
	public String protocol;
	public TagHierarchyNode tagHierarchy;
	public SyncUpdateMethod syncMethod;
	public Integer syncInterval;
	public Date lastSyncRun;
	public Boolean newMessages;
	public Boolean trustInvalidSSLCertificates;
	public Timestamp version;
	public String deleteMode;
}

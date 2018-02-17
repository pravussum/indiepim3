export enum ENCRYPTION_TYPE {NONE = "NONE", STARTTLS = "STARTTLS", TLS = "TLS", SSL = "SSL"}

export enum AUTHENTICATION_TYPE {NONE = "NONE", PASSWORD_NORMAL = "PASSWORD_NORMAL"}

export enum SYNC_UPDATE_METHOD {FLAGS = "FLAGS", FULL = "FULL"}

export class MessageAccount {
  id?: number;
  email?: string;
  accountName: string;
  userName?: string;
  host?: string;
  outgoingHost?: string;
  port?: string;
  outgoingPort?: string;
  authentication?: AUTHENTICATION_TYPE;
  outgoingAuthentication?: AUTHENTICATION_TYPE;
  encryption?: ENCRYPTION_TYPE;
  outgoingEncryption?: ENCRYPTION_TYPE;
  tag?: string;
  password?: string;
  tagHierarchy?: string;
  syncMethod?: SYNC_UPDATE_METHOD;
  syncInterval?: number;
  lastSyncRun?: string;
  newMessages?: string;
  trustInvalidSSLCertificates?: string;
  version?: string;
  syncProgress?: string;
  syncProgressValue?: string;
  syncActive?: string;
}

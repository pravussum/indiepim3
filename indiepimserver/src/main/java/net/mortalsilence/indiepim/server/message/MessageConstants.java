package net.mortalsilence.indiepim.server.message;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public interface MessageConstants {

    String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    String CONTENT_TYPE_TEXT_PLAIN_UTF8 = CONTENT_TYPE_TEXT_PLAIN + "; charset=utf-8";
	String CONTENT_TYPE_TEXT_HTML = "text/html";
    String CONTENT_TYPE_TEXT_HTML_UTF8 = CONTENT_TYPE_TEXT_HTML + "; charset=utf-8";
    String CONTENT_TYPE_TEXT_ALL = "text/*";
    String CONTENT_TYPE_IMAGE_ALL = "image/*";
    String CONTENT_TYPE_MULTIPART_ALL = "multipart/*";
	String CONTENT_TYPE = "Content-Type:";
    String CONTENT_TYPE_PARAM_CHARSET = "charset";

	String PROTOCOL_IMAP = "IMAP";
	String PROTOCOL_POP3 = "POP3";
	
	String DATETIME_FORMAT_EUR = "yyyy-MM-dd HH.mm.ss";

    String DEFAULT_FOLDER_SENT = "Sent";
    String DEFAULT_FOLDER_TRASH = "Trash";
    String DEFAULT_FOLDER_JUNK = "Junk";
    String DEFAULT_FOLDER_DRAFTS = "Drafts";
    String DEFAULT_FOLDER_INBOX = "INBOX";

    String DISPOSITION_ATTACHMENT = "attachment";
    String DISPOSITION_INLINE = "inline";
}

package net.mortalsilence.indiepim.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.mortalsilence.indiepim.server.domain.AttachmentPO;
import org.joda.time.format.ISODateTimeFormat;

import java.io.Serializable;
import java.util.*;

public class MessageDTO implements Serializable {

	public Long id;
	public String subject;
	public Set<TagDTO> tags = new TreeSet<TagDTO>();
	public String sender;
    public String senderEmail;
	public List<String> receiver;
	public List<String> cc;
	public List<String> bcc;
    @JsonIgnore public Date dateReceived;
    @JsonProperty("dateReceived") public String getDateReceived() {
        if(dateReceived == null)
            return null;
        return ISODateTimeFormat.dateTime().print(dateReceived.getTime());
    }
	public String contentHtml;
	public String contentText;
	public Long accountId;
	public Boolean read;
    public Boolean deleted;
    public Boolean draft;
	public Boolean hasAttachment;
	public Integer star;
    public List<AttachmentDTO> attachments = new LinkedList<AttachmentDTO>();
}

package net.mortalsilence.indiepim.server.command.results;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.mortalsilence.indiepim.server.pushmessage.PushMessage;
import net.mortalsilence.indiepim.server.command.Result;

import java.util.Collection;

public class CometMessagesResult implements Result {

	@JsonProperty("cometMessages") private Collection<PushMessage> pushMessages;
    @JsonProperty("isHeartbeat") private boolean isHeartbeat;
    @JsonProperty("isError") private boolean isError;
    @JsonProperty("errorMessage") private String errorMessage;

    @JsonIgnore
    public boolean isHeartbeat() {
		return isHeartbeat;
	}

	public void setHeartbeat(boolean isHeartbeat) {
		this.isHeartbeat = isHeartbeat;
	}

    @JsonIgnore
    public boolean isError() {
		return isError;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}

    @JsonIgnore
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

    @JsonIgnore
	public CometMessagesResult(Collection<PushMessage> messages) {
		super();
		this.pushMessages = messages;
	}

	public CometMessagesResult() {
	}

    @JsonIgnore
    public Collection<PushMessage> getPushMessages() {
        return pushMessages;
    }

    public void setPushMessages(Collection<PushMessage> pushMessages) {
        this.pushMessages = pushMessages;
    }
}

package net.mortalsilence.indiepim.server.pushmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public interface PushMessage extends Serializable {

    @JsonProperty String getMessageType();
}

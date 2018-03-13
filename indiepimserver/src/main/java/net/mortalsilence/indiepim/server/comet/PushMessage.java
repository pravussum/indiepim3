package net.mortalsilence.indiepim.server.comet;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public interface PushMessage extends Serializable {

    @JsonProperty String getMessageType();
}

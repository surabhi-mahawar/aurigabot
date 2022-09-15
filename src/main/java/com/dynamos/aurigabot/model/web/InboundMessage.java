package com.dynamos.aurigabot.model.web;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InboundMessage {
    String messageId;

    @JsonAlias({"body"})
    String body;

    @JsonAlias({"from"})
    String from;
}

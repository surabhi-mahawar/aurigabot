package com.aurigabot.dto.netcore.whatsapp.inbound;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {
    @JsonAlias({"incoming_message", "delivery_status"})
    private SingleInboundMessage[] messages;
}

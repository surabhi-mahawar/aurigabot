package com.dynamos.aurigabot.model.web;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutboundMessage {
    private OutbondMessagePayload message;
    private String to;
    private String messageId;
}

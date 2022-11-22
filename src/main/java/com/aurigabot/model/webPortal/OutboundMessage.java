package com.aurigabot.model.webPortal;

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

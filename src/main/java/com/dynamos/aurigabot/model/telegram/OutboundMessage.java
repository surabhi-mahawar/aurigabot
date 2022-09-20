package com.dynamos.aurigabot.model.telegram;

import com.dynamos.aurigabot.model.webPortal.OutbondMessagePayload;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutboundMessage {
    private String text;
    private String chatId;
}

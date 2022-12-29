package com.aurigabot.model.telegram;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutboundMessage {
    private String text;
    private String chatId;

    private String parseMode;
}

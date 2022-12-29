package com.aurigabot.dto.netcore.whatsapp.inbound;

import com.sun.istack.Nullable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageContext {
    @Nullable
    private String ncmessage_id;

    @Nullable
    private String message_id;
}

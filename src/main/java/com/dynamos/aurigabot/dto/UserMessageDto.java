package com.dynamos.aurigabot.dto;

import com.dynamos.aurigabot.enums.ChannelProvider;
import com.dynamos.aurigabot.enums.MessageChannel;
import com.dynamos.aurigabot.enums.UserMessageStatus;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.UUID;

@Getter
@Setter
@Builder
public class UserMessageDto {
    private String fromSource;
    private String toSource;
    private UUID fromUserId;
    private UUID toUserId;
    private MessageChannel channel;
    private ChannelProvider provider;
    private UUID commandId;
    private UUID flowId;
    private int index;
    private String message;

    @Type(type = "jsonb")
    @JsonSerialize
    @JsonDeserialize
    private MessagePayloadDto payload;

    private UserMessageStatus status;
}

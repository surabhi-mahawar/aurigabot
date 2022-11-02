package com.dynamos.aurigabot.dto;

import com.dynamos.aurigabot.entity.Flow;
import com.dynamos.aurigabot.enums.ChannelProvider;
import com.dynamos.aurigabot.enums.MessageChannel;
import com.dynamos.aurigabot.enums.UserMessageStatus;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMessageDto {
    private String fromSource;
    private String toSource;
    private UUID fromUserId;
    private UUID toUserId;
    private MessageChannel channel;
    private ChannelProvider provider;
    private UUID commandId;
    private Flow flowId;
    private int index;
    private String message;

//    private Boolean msgFound;

    @Type(type = "jsonb")
    @JsonSerialize
    @JsonDeserialize
    private MessagePayloadDto payload;

    private UserMessageStatus status;
}

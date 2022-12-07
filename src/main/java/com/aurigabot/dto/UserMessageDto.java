package com.aurigabot.dto;

import com.aurigabot.entity.Flow;
import com.aurigabot.enums.ChannelProvider;
import com.aurigabot.enums.MessageChannel;
import com.aurigabot.enums.UserMessageStatus;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMessageDto implements Serializable {
    private String fromSource;
    private String toSource;
    private UUID fromUserId;
    private UUID toUserId;
    private MessageChannel channel;
    private ChannelProvider provider;
    private UUID commandId;
    private Flow flow;
    private int index;
    private String message;

//    private Boolean msgFound;

    @Type(type = "jsonb")
    @JsonSerialize
    @JsonDeserialize
    private MessagePayloadDto payload;

    private UserMessageStatus status;
}

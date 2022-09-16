package com.dynamos.aurigabot.entity;

import com.dynamos.aurigabot.enums.UserMessageStatus;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Builder
@Data
@Table(name = "userMessage")
public class UserMessage {
    @Id
    private UUID id;
    private String fromSource;
    private String toSource;
    private UUID fromUserId;
    private UUID toUserId;
    private String channel;
    private String provider;
    private UUID commandId;
    private UUID flowId;
    private int index;
    private String message;
    private UserMessageStatus status;
}

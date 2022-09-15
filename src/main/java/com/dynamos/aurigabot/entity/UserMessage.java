package com.dynamos.aurigabot.entity;

import com.dynamos.aurigabot.enums.Status;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigInteger;
import java.util.UUID;

@Builder
@Data
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
    private String message;
    private Status status;
}

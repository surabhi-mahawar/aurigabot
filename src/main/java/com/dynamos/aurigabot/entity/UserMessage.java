package com.dynamos.aurigabot.entity;

import com.dynamos.aurigabot.enums.Status;
import com.dynamos.aurigabot.enums.UserMessageStatus;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.UUID;

@Builder
@Data
public class UserMessage {
    @Id
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "command_id")
    private Commands commands;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="flow_id")
    private Flow flow;

    private String fromSource;
    private String toSource;
    private UUID fromUserId;
    private UUID toUserId;
    private String channel;
    private String provider;

    private String message;
    @Enumerated(EnumType.STRING)
    private UserMessageStatus status;
}

package com.dynamos.aurigabot.entity;

import com.dynamos.aurigabot.enums.Status;
import com.dynamos.aurigabot.enums.UserMessageStatus;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.r2dbc.postgresql.codec.Json;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.UUID;

@Builder
@Data
@Table(name = "user_message")
public class UserMessage {
    @Id
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "command_id")
    private Command command;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="flow_id")
    private Flow flow;

    private String fromSource;
    private String toSource;
    private UUID fromUserId;
    private UUID toUserId;
    private String channel;
    private String provider;
    private int index;
    private String message;

    @Type(type = "jsonb")
    @JsonSerialize
    @JsonDeserialize
    private Json payload;

    @Enumerated(EnumType.STRING)
    private UserMessageStatus status;
}

package com.dynamos.aurigabot.entity;

import com.dynamos.aurigabot.enums.UserMessageStatus;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.r2dbc.postgresql.codec.Json;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.util.UUID;

@Builder
@Data
@Table(name = "user_message")
public class UserMessage {
    @Id
    private UUID id;
    private String fromSource;
    private String toSource;
    private UUID fromUserId;
    private UUID toUserId;
    private String channel;
    private String provider;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "command_id")
    private UUID commandId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="flow_id")
    private UUID flowId;

    private int index;
    private String message;

    @Type(type = "jsonb")
    @JsonSerialize
    @JsonDeserialize
    private Json payload;

    private UserMessageStatus status;
}

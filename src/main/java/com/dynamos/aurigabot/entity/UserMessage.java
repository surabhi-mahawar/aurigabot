package com.dynamos.aurigabot.entity;

import com.dynamos.aurigabot.enums.ChannelProvider;
import com.dynamos.aurigabot.enums.MessageChannel;
import com.dynamos.aurigabot.enums.Status;
import com.dynamos.aurigabot.enums.UserMessageStatus;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.r2dbc.postgresql.codec.Json;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.persistence.*;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Setter
@Getter
@Builder
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_message")
public class UserMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @Column(name = "flow")
    private Flow flow;

    private String fromSource;
    private String toSource;
    private UUID fromUserId;
    private UUID toUserId;

    @Enumerated(EnumType.STRING)
    private MessageChannel channel;

    @Enumerated(EnumType.STRING)
    private ChannelProvider provider;

    private int index;
    private String message;

    @Type(type = "jsonb")
    @JsonSerialize
    @JsonDeserialize
    private Json payload;

    @Enumerated(EnumType.STRING)
    private UserMessageStatus status;

    @CreatedDate
    protected LocalDateTime createdAt;

    private LocalDateTime sentAt;
    private LocalDateTime receivedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
}

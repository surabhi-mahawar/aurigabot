package com.aurigabot.entity;

import com.aurigabot.dto.MessagePayloadDto;
import com.aurigabot.enums.ChannelProvider;
import com.aurigabot.enums.MessageChannel;
import com.aurigabot.enums.UserMessageStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.r2dbc.postgresql.codec.Json;
import lombok.*;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
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
    private MessagePayloadDto payload;

    @Enumerated(EnumType.STRING)
    private UserMessageStatus status;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime sentAt;


    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime receivedAt;


    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime deliveredAt;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime readAt;
}

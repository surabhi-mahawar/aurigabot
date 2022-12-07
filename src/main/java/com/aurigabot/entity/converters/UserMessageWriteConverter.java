package com.aurigabot.entity.converters;

import com.aurigabot.entity.UserMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.r2dbc.core.Parameter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UserMessage entity write converter
 * To resolve issue of "nested entities not supported", R2dbc does not support the nested entities.
 */
@WritingConverter
public class UserMessageWriteConverter implements Converter<UserMessage, OutboundRow> {

    @Override
    public OutboundRow convert(UserMessage userMessage) {

        OutboundRow row = new OutboundRow();
        row.put("id", Parameter.from(UUID.randomUUID()));
        if(userMessage.getFlow() != null) {
            row.put("flow", Parameter.from(userMessage.getFlow().getId()));
        }

        LocalDateTime createdAt;
        if(userMessage.getCreatedAt() != null) {
            createdAt = userMessage.getCreatedAt();
        } else {
            createdAt = LocalDateTime.now();
        }

        row.put("index", Parameter.fromOrEmpty(userMessage.getIndex(), Integer.class));
        row.put("from_user_id", Parameter.fromOrEmpty(userMessage.getFromUserId(), UUID.class));
        row.put("from_source", Parameter.fromOrEmpty(userMessage.getFromSource(), String.class));
        row.put("to_user_id", Parameter.fromOrEmpty(userMessage.getToUserId(), UUID.class));
        row.put("to_source", Parameter.fromOrEmpty(userMessage.getToSource(), String.class));
        row.put("channel", Parameter.fromOrEmpty(userMessage.getChannel(), String.class));
        row.put("provider", Parameter.fromOrEmpty(userMessage.getProvider(), String.class));
        row.put("message", Parameter.fromOrEmpty(userMessage.getMessage(), String.class));
//        row.put("payload", Parameter.fromOrEmpty(userMessage.getPayload(), Json.class));
        row.put("status", Parameter.fromOrEmpty(userMessage.getStatus(), String.class));
        row.put("received_at", Parameter.fromOrEmpty(userMessage.getReceivedAt(), LocalDateTime.class));
        row.put("sent_at", Parameter.fromOrEmpty(userMessage.getSentAt(), LocalDateTime.class));
        row.put("delivered_at", Parameter.fromOrEmpty(userMessage.getDeliveredAt(), LocalDateTime.class));
        row.put("read_at", Parameter.fromOrEmpty(userMessage.getReadAt(), LocalDateTime.class));
        row.put("created_at", Parameter.from(createdAt));

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        try {
            String json = ow.writeValueAsString(userMessage.getPayload());
            row.put("payload", Parameter.fromOrEmpty(Json.of(json), Json.class));
        } catch(JsonProcessingException ex) {

        }

        return row;
    }


}

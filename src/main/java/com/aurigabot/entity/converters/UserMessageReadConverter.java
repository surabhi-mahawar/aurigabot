package com.aurigabot.entity.converters;

import com.aurigabot.dto.FlowPayloadDto;
import com.aurigabot.dto.MessagePayloadDto;
import com.aurigabot.entity.UserMessage;
import com.aurigabot.entity.Flow;
import com.aurigabot.enums.ChannelProvider;
import com.aurigabot.enums.CommandType;
import com.aurigabot.enums.MessageChannel;
import com.aurigabot.enums.UserMessageStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.r2dbc.postgresql.codec.Json;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.r2dbc.core.Parameter;

import java.time.LocalDateTime;
import java.util.UUID;

@ReadingConverter
public class UserMessageReadConverter implements Converter<Row, UserMessage> {

    @Override
    public UserMessage convert(Row source) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        Flow flow;
        if(source.get("flow", UUID.class) != null) {
            FlowPayloadDto flowPayloadDto = null;
            try {
                flowPayloadDto = mapper.readValue(source.get("fl_payload", String.class), FlowPayloadDto.class);
            } catch(JsonProcessingException ex) {

            }
            flow = Flow.builder().id(source.get("fl_id", UUID.class))
                    .commandType(CommandType.getEnumByValue(source.get("fl_command_type", String.class)))
                    .question(source.get("fl_question", String.class))
                    .index(source.get("fl_index", Integer.class))
//                    .payload(source.get("fl_payload", FlowPayloadDto.class))
                    .payload(flowPayloadDto)
                    .build();
        } else {
            flow = null;
        }

        MessagePayloadDto messagePayloadDto = null;
        try {
            messagePayloadDto = mapper.readValue(source.get("payload", String.class), MessagePayloadDto.class);
        } catch(JsonProcessingException ex) {

        }

        return UserMessage.builder()
                .id(source.get("id", UUID.class))
                .flow(flow)
                .index(source.get("index", Integer.class))
                .fromUserId(source.get("from_user_id", UUID.class))
                .fromSource(source.get("from_source", String.class))
                .toUserId(source.get("to_user_id", UUID.class))
                .toSource(source.get("to_source", String.class))
                .channel(MessageChannel.valueOf(source.get("channel", String.class)))
                .provider(ChannelProvider.valueOf(source.get("provider", String.class)))
                .message(source.get("message", String.class))
//                .payload(source.get("payload", MessagePayloadDto.class))
                .payload(messagePayloadDto)
                .status(UserMessageStatus.valueOf(source.get("status", String.class)))
                .receivedAt(source.get("received_at", LocalDateTime.class))
                .sentAt(source.get("sent_at", LocalDateTime.class))
                .deliveredAt(source.get("delivered_at", LocalDateTime.class))
                .readAt(source.get("read_at", LocalDateTime.class))
                .createdAt(source.get("created_at", LocalDateTime.class))
                .build();
    }
}
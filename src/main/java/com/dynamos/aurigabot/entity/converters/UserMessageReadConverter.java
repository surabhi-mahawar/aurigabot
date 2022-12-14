package com.dynamos.aurigabot.entity.converters;

import com.dynamos.aurigabot.entity.Flow;
import com.dynamos.aurigabot.entity.UserMessage;
import com.dynamos.aurigabot.enums.ChannelProvider;
import com.dynamos.aurigabot.enums.CommandType;
import com.dynamos.aurigabot.enums.MessageChannel;
import com.dynamos.aurigabot.enums.UserMessageStatus;
import io.r2dbc.postgresql.codec.Json;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.LocalDateTime;
import java.util.UUID;

@ReadingConverter
public class UserMessageReadConverter implements Converter<Row, UserMessage> {

    @Override
    public UserMessage convert(Row source) {
        Flow flow;
        if(source.get("flow", UUID.class) != null) {
            flow = Flow.builder().id(source.get("fl_id", UUID.class))
                    .commandType(CommandType.getEnumByValue(source.get("fl_command_type", String.class)))
                    .question(source.get("fl_question", String.class))
                    .index(source.get("fl_index", Integer.class))
                    .payload(source.get("fl_payload", Json.class))
                    .build();
        } else {
            flow = null;
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
                .payload(source.get("payload", Json.class))
                .status(UserMessageStatus.valueOf(source.get("status", String.class)))
                .receivedAt(source.get("received_at", LocalDateTime.class))
                .sentAt(source.get("sent_at", LocalDateTime.class))
                .deliveredAt(source.get("delivered_at", LocalDateTime.class))
                .readAt(source.get("read_at", LocalDateTime.class))
                .createdAt(source.get("created_at", LocalDateTime.class))
                .build();
    }
}
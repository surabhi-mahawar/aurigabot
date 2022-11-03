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
public class FlowReadConverter implements Converter<Row, Flow> {

    @Override
    public Flow convert(Row source) {
        return Flow.builder().id(source.get("id", UUID.class))
                .commandType(CommandType.getEnumByValue(source.get("command_type", String.class)))
                .question(source.get("question", String.class))
                .index(source.get("index", Integer.class))
                .payload(source.get("payload", Json.class))
                .build();
    }
}

package com.aurigabot.entity.converters;

import com.aurigabot.dto.FlowPayloadDto;
import com.aurigabot.dto.MessagePayloadDto;
import com.aurigabot.entity.Flow;
import com.aurigabot.enums.CommandType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.r2dbc.postgresql.codec.Json;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.UUID;

/**
 * Flow read converter
 */
@ReadingConverter
public class FlowReadConverter implements Converter<Row, Flow> {

    @Override
    public Flow convert(Row source) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();

        FlowPayloadDto flowPayloadDto = null;
        try {
            flowPayloadDto = mapper.readValue(source.get("payload", String.class), FlowPayloadDto.class);
        } catch(JsonProcessingException ex) {

        }
        return Flow.builder().id(source.get("id", UUID.class))
                .commandType(CommandType.getEnumByValue(source.get("command_type", String.class)))
                .question(source.get("question", String.class))
                .index(source.get("index", Integer.class))
                .payload(flowPayloadDto)
                .build();
    }
}

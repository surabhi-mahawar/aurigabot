package com.dynamos.aurigabot.utils;

import com.dynamos.aurigabot.dto.UserMessageDto;
import com.dynamos.aurigabot.entity.UserMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.r2dbc.postgresql.codec.Json;

public class UserMessageUtil {
    /**
     * Convert userMessageDto object to userMessage entity object
     * @param userMessageDto
     * @return
     */
    public static UserMessage convertDtotoDao(UserMessageDto userMessageDto) {
        UserMessage userMessage = UserMessage.builder()
                .fromSource(userMessageDto.getFromSource())
                .toSource(userMessageDto.getToSource())
                .fromUserId(userMessageDto.getFromUserId())
                .toUserId(userMessageDto.getToUserId())
                .channel(userMessageDto.getChannel())
                .provider(userMessageDto.getProvider())
                .message(userMessageDto.getMessage())
                .status(userMessageDto.getStatus())
                .command(null)
                .flow(null)
                .index(userMessageDto.getIndex())
                .build();


        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        try {
            String json = ow.writeValueAsString(userMessageDto.getPayload());
            userMessage.setPayload(Json.of(json));
        } catch(JsonProcessingException ex) {

        }

        return userMessage;
    }
}

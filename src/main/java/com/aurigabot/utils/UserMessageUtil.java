package com.aurigabot.utils;

import com.aurigabot.entity.UserMessage;
import com.aurigabot.repository.FlowRepository;
import com.aurigabot.dto.UserMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.beans.factory.annotation.Autowired;

public class UserMessageUtil {

    @Autowired
    private static FlowRepository flowRepository;
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
                .flow(userMessageDto.getFlow())
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

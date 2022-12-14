package com.dynamos.aurigabot.adapters;

import com.dynamos.aurigabot.dto.UserMessageDto;
import com.dynamos.aurigabot.entity.UserMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.publisher.Mono;

public interface AbstractAdapter {
    /**
     * Converts inbound message object to UserMessage object
     * @param message
     * @return UserMessageDto
     */
    public Mono<UserMessageDto> convertInboundMsgToMessageFormat(Object message);

    /**
     * Send user message to user
     * @param userMessageDto
     * @return
     */
    public Mono<UserMessageDto> sendOutboundMessage(UserMessageDto userMessageDto);
}

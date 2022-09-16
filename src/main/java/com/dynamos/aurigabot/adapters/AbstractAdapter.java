package com.dynamos.aurigabot.adapters;

import com.dynamos.aurigabot.entity.UserMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.publisher.Mono;

public interface AbstractAdapter {
    /**
     * Converts inbound message object to UserMessage object
     * @param message
     * @return UserMessage
     */
    public Mono<UserMessage> convertInboundMsgToMessageFormat(Object message);

    /**
     * Converts user message object to outbound message format
     * @param userMessage
     * @return UserMessage
     */
    public Object convertOutboundMsgFromMessageFormat(UserMessage userMessage);

    /**
     * Send user message to user
     * @param userMessage
     * @return
     */
    public Mono<UserMessage> sendOutboundMessage(UserMessage userMessage);
}

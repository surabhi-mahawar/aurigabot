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

    public Mono<UserMessage> convertOutboundMsgFromMessageFormat(UserMessage message);
}

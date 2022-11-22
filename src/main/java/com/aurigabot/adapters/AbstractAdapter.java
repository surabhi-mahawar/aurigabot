package com.aurigabot.adapters;

import com.aurigabot.dto.UserMessageDto;
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

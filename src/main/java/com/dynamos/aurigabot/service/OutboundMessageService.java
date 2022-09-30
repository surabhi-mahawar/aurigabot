package com.dynamos.aurigabot.service;

import com.dynamos.aurigabot.adapters.AbstractAdapter;
import com.dynamos.aurigabot.dto.UserMessageDto;
import com.dynamos.aurigabot.entity.UserMessage;
import com.dynamos.aurigabot.enums.UserMessageStatus;
import com.dynamos.aurigabot.repository.FlowRepository;
import com.dynamos.aurigabot.repository.UserMessageRepository;
import com.dynamos.aurigabot.response.HttpApiResponse;
import com.dynamos.aurigabot.utils.UserMessageUtil;
import lombok.Builder;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Builder
public class OutboundMessageService {
    private AbstractAdapter adapter;
    private UserMessageRepository userMessageRepository;


    /**
     * Process outbound message - convert to channel message format and send message to user
     * @param response
     * @param userMessageDto
     * @return
     */
    public Mono<HttpApiResponse> processOutboundMessage(HttpApiResponse response, UserMessageDto userMessageDto) {
        return adapter.sendOutboundMessage(userMessageDto).map(new Function<UserMessageDto, Mono<HttpApiResponse>>() {
            @Override
            public Mono<HttpApiResponse> apply(UserMessageDto userMessageDto) {
                UserMessage userMessageDao = UserMessageUtil.convertDtotoDao(userMessageDto);
                return userMessageRepository.save(userMessageDao).map(new Function<UserMessage, HttpApiResponse>() {
                    @Override
                    public HttpApiResponse apply(UserMessage userMessage) {
                        if(userMessage.getStatus().equals(UserMessageStatus.SENT)) {
                            response.setMessage("Reply message sent to user.");
                        } else {
                            response.setMessage("Reply message not sent to user.");
                        }

                        return response;
                    }
                });
            }
        }).flatMap(new Function<Mono<HttpApiResponse>, Mono<? extends HttpApiResponse>>() {
            @Override
            public Mono<? extends HttpApiResponse> apply(Mono<HttpApiResponse> m1) {
                return m1;
            }
        });
    }
}

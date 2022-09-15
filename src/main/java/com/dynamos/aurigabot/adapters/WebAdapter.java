package com.dynamos.aurigabot.adapters;

import com.dynamos.aurigabot.entity.UserMessage;
import com.dynamos.aurigabot.model.web.InboundMessage;
import com.dynamos.aurigabot.model.web.OutbondMessagePayload;
import com.dynamos.aurigabot.model.web.OutboundMessage;
import com.dynamos.aurigabot.model.web.OutboundResponse;
import com.dynamos.aurigabot.service.WebService;
import com.dynamos.aurigabot.utils.BotUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class WebAdapter implements AbstractAdapter {

    /**
     * Convert inbound WebMessage to UserMessage
     * @param message
     * @return
     */
    public Mono<UserMessage> convertInboundMsgToMessageFormat(Object message){
        InboundMessage webMsg = (InboundMessage) message;
        UserMessage userMsg = UserMessage.builder().build();
        userMsg.setFromSource(webMsg.getFrom());
        userMsg.setMessage(webMsg.getBody());
        userMsg.setToSource(BotUtil.USER_ADMIN);
//        userMsg.setToUserId(1);
//        userMsg.setMessageId(BotUtil.getUserMessageId(webMsg.getMessageId()));
        userMsg.setProvider(BotUtil.PROVIDER_TRANSPORT_SOCKET);
        userMsg.setChannel(BotUtil.CHANNEL_WEB);
        return Mono.just(userMsg);
    }

    public Mono<UserMessage> convertOutboundMsgFromMessageFormat(UserMessage userMessage){
        OutbondMessagePayload payload = OutbondMessagePayload.builder()
                .title("Welcome to auriga bot.")
                .msg_type("text")
                .build();
        OutboundMessage outboundMessage = OutboundMessage.builder()
                .message(payload)
                .to(userMessage.getToSource())
                .messageId(BotUtil.getUserMessageId(""))
                .build();

        return (new WebService()).sendOutboundMessage("http://localhost:3005/botMsg/adapterOutbound", outboundMessage)
                .map(new Function<OutboundResponse, UserMessage>() {
                    @Override
                    public UserMessage apply(OutboundResponse outboundResponse) {
                        return userMessage;
                    }
                });
    }
}

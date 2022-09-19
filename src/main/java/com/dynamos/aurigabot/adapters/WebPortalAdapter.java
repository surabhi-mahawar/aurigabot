package com.dynamos.aurigabot.adapters;

import com.dynamos.aurigabot.dto.UserMessageDto;
import com.dynamos.aurigabot.entity.UserMessage;
import com.dynamos.aurigabot.enums.MessagePayloadType;
import com.dynamos.aurigabot.enums.UserMessageStatus;
import com.dynamos.aurigabot.dto.MessagePayloadDto;
import com.dynamos.aurigabot.model.webPortal.InboundMessage;
import com.dynamos.aurigabot.model.webPortal.OutbondMessagePayload;
import com.dynamos.aurigabot.model.webPortal.OutboundMessage;
import com.dynamos.aurigabot.response.webPortal.OutboundResponse;
import com.dynamos.aurigabot.service.WebPortalService;
import com.dynamos.aurigabot.utils.BotUtil;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class WebPortalAdapter implements AbstractAdapter {

    /**
     * Convert received WebMessage object to UserMessage object
     * @param message
     * @return
     */
    public Mono<UserMessageDto> convertInboundMsgToMessageFormat(Object message){
        InboundMessage webMsg = (InboundMessage) message;

        MessagePayloadDto payload = MessagePayloadDto.builder()
                .message(webMsg.getBody())
                .msgType(MessagePayloadType.TEXT)
                .build();

        UserMessageDto userMsg = UserMessageDto.builder().build();
        userMsg.setFromSource(webMsg.getFrom());
        userMsg.setMessage(webMsg.getBody());
        userMsg.setPayload(payload);
        userMsg.setProvider(BotUtil.PROVIDER_TRANSPORT_SOCKET);
        userMsg.setChannel(BotUtil.CHANNEL_WEB);
        userMsg.setStatus(UserMessageStatus.REPLIED);
//        userMsg.setIndex(0);
        return Mono.just(userMsg);
    }

    /**
     * Convert UserMessage object to OutboundMessage object
     * @param userMessage
     * @return
     */
    public Object convertOutboundMsgFromMessageFormat(UserMessageDto userMessage) {
        OutbondMessagePayload payload = OutbondMessagePayload.builder()
                .title(userMessage.getMessage())
                .msg_type("text")
                .build();
        if(userMessage.getPayload().getChoices() != null) {
            payload.setChoices(userMessage.getPayload().getChoices());
        }

        OutboundMessage outboundMessage = OutboundMessage.builder()
                .message(payload)
                .to(userMessage.getToSource())
                .messageId(BotUtil.getUserMessageId(""))
                .build();

        return outboundMessage;
    }

    /**
     * Send outbound message to web portal
     * @param userMessage
     * @return
     */
    public Mono<UserMessageDto> sendOutboundMessage(UserMessageDto userMessage) {
        OutboundMessage outboundMessage = (OutboundMessage) convertOutboundMsgFromMessageFormat(userMessage);

        return (new WebPortalService()).sendOutboundMessage("http://localhost:3005/botMsg/adapterOutbound", outboundMessage)
                .map(new Function<OutboundResponse, UserMessageDto>() {
                    @Override
                    public UserMessageDto apply(OutboundResponse outboundResponse) {
                        userMessage.setStatus(UserMessageStatus.SENT);
                        return userMessage;
                    }
                });
    }
}

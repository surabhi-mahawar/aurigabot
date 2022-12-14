package com.dynamos.aurigabot.adapters;

import com.dynamos.aurigabot.dto.UserMessageDto;
import com.dynamos.aurigabot.enums.ChannelProvider;
import com.dynamos.aurigabot.enums.MessageChannel;
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

import java.util.ArrayList;
import java.util.function.Function;

public class WebPortalAdapter implements AbstractAdapter {
    private String outboundUrl;

    public WebPortalAdapter(String outboundUrl) {
        this.outboundUrl = outboundUrl;
    }

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
        userMsg.setProvider(ChannelProvider.TRANSPORT_SOCKET);
        userMsg.setChannel(MessageChannel.WEB);
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
        if(userMessage.getPayload() != null && userMessage.getPayload().getChoices() != null) {
            ArrayList choices = new ArrayList();
            userMessage.getPayload().getChoices().forEach(item -> {
                item.setKey("");
                choices.add(item);
            });

            payload.setChoices(choices);
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
     * @param userMessageDto
     * @return
     */
    public Mono<UserMessageDto> sendOutboundMessage(UserMessageDto userMessageDto) {
        OutboundMessage outboundMessage = (OutboundMessage) convertOutboundMsgFromMessageFormat(userMessageDto);

        return (new WebPortalService()).sendOutboundMessage(this.outboundUrl, outboundMessage)
                .map(new Function<OutboundResponse, UserMessageDto>() {
                    @Override
                    public UserMessageDto apply(OutboundResponse outboundResponse) {
                        if(outboundResponse.getStatus().equals("OK")) {
                            userMessageDto.setStatus(UserMessageStatus.SENT);
                        } else {
                            userMessageDto.setStatus(UserMessageStatus.NOT_SENT);
                        }
                        return userMessageDto;
                    }
                });
    }
}

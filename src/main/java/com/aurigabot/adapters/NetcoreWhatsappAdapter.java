package com.aurigabot.adapters;

import com.aurigabot.dto.MessagePayloadDto;
import com.aurigabot.dto.UserMessageDto;
import com.aurigabot.dto.netcore.whatsapp.inbound.Message;
import com.aurigabot.dto.netcore.whatsapp.inbound.SingleInboundMessage;
import com.aurigabot.dto.netcore.whatsapp.outbound.OutboundMessage;
import com.aurigabot.dto.netcore.whatsapp.outbound.SingleOutboundMessage;
import com.aurigabot.dto.netcore.whatsapp.outbound.Text;
import com.aurigabot.enums.ChannelProvider;
import com.aurigabot.enums.MessageChannel;
import com.aurigabot.enums.MessagePayloadType;
import com.aurigabot.enums.UserMessageStatus;
import com.aurigabot.enums.netcore.whatsapp.MessageType;
import com.aurigabot.response.netcore.whatsapp.OutboundResponse;
import com.aurigabot.service.outbound.NetcoreWhatsappService;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.function.Function;

public class NetcoreWhatsappAdapter implements AbstractAdapter{
    public String token;
    public String baseUrl;
    public String whatsappNumber;
    public String sourceId;

    public NetcoreWhatsappAdapter(String baseUrl, String token, String sourceId, String whatsappNumber) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.sourceId = sourceId;
        this.whatsappNumber = whatsappNumber;
    }

    /**
     * Convert received Telegram Message object to UserMessage object
     * @param message
     * @return
     */
    public Mono<UserMessageDto> convertInboundMsgToMessageFormat(Object message){
        Message whatsappMsgs = (Message) message;
        try{
            SingleInboundMessage whatsappMsg = whatsappMsgs.getMessages()[0];
            MessagePayloadDto payload = MessagePayloadDto.builder()
                    .message(whatsappMsg.getText().getText())
                    .msgType(MessagePayloadType.TEXT)
                    .build();

            UserMessageDto userMsg = UserMessageDto.builder().build();
            userMsg.setFromSource(whatsappMsg.getMobile().substring(2));
            userMsg.setMessage(whatsappMsg.getText().getText());
            userMsg.setPayload(payload);
            userMsg.setProvider(ChannelProvider.NETCORE);
            userMsg.setChannel(MessageChannel.WHATSAPP);
            userMsg.setStatus(UserMessageStatus.REPLIED);
            return Mono.just(userMsg);
        } catch (Exception ex) {
            return Mono.just(null);
        }
    }

    /**
     * Convert UserMessage object to OutboundMessage object
     * @param userMessage
     * @return
     */
    public Object convertOutboundMsgFromMessageFormat(UserMessageDto userMessage) {
        StringBuilder text = new StringBuilder(userMessage.getMessage());
        if(userMessage.getPayload() != null && userMessage.getPayload().getChoices() != null) {
            ArrayList choices = new ArrayList();
            userMessage.getPayload().getChoices().forEach(item -> {
                text.append("\n").append(item.getText());
            });
        }

        Text t = Text.builder().content(text.toString()).previewURL("false").build();
        Text[] texts = {t};

        SingleOutboundMessage singleOutboundMessage = SingleOutboundMessage
                .builder()
                .from(this.sourceId)
                .to("91"+userMessage.getToSource())
                .recipientType("individual")
                .messageType(MessageType.TEXT.toString())
                .header("custom_data")
                .text(texts)
                .build();

        return OutboundMessage.builder().message(new SingleOutboundMessage[]{singleOutboundMessage})
                .build();
    }

    /**
     * Send outbound message to telegram
     * @param userMessageDto
     * @return
     */
    public Mono<UserMessageDto> sendOutboundMessage(UserMessageDto userMessageDto) {
        OutboundMessage outboundMessage = (OutboundMessage) convertOutboundMsgFromMessageFormat(userMessageDto);

        return (new NetcoreWhatsappService(baseUrl, token)).sendOutboundMessage(outboundMessage)
                .map(new Function<OutboundResponse, UserMessageDto>() {
                    @Override
                    public UserMessageDto apply(OutboundResponse outboundResponse) {
                        if(outboundResponse.getStatus().equals("success")) {
                            userMessageDto.setStatus(UserMessageStatus.SENT);
                        } else {
                            userMessageDto.setStatus(UserMessageStatus.NOT_SENT);
                        }
                        return userMessageDto;
                    }
                });
    }
}

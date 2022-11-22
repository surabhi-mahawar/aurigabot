package com.aurigabot.adapters;

import com.aurigabot.enums.ChannelProvider;
import com.aurigabot.enums.MessageChannel;
import com.aurigabot.response.telegram.OutboundResponse;
import com.aurigabot.service.TelegramOutboundService;
import com.aurigabot.dto.MessagePayloadDto;
import com.aurigabot.dto.UserMessageDto;
import com.aurigabot.enums.MessagePayloadType;
import com.aurigabot.enums.UserMessageStatus;
import com.aurigabot.model.telegram.OutboundMessage;
import reactor.core.publisher.Mono;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.function.Function;

public class TelegramAdapter  implements AbstractAdapter {
    private String outboundBaseUrl;

    public TelegramAdapter(String outboundBaseUrl) {
        this.outboundBaseUrl = outboundBaseUrl;
    }

    /**
     * Convert received Telegram Message object to UserMessage object
     * @param message
     * @return
     */
    public Mono<UserMessageDto> convertInboundMsgToMessageFormat(Object message){
        Message telegramMsg = (Message) message;

        MessagePayloadDto payload = MessagePayloadDto.builder()
                .message(telegramMsg.getText())
                .msgType(MessagePayloadType.TEXT)
                .build();

        UserMessageDto userMsg = UserMessageDto.builder().build();
        userMsg.setFromSource(telegramMsg.getFrom().getId().toString());
        userMsg.setMessage(telegramMsg.getText());
        userMsg.setPayload(payload);
        userMsg.setProvider(ChannelProvider.TELEGRAM);
        userMsg.setChannel(MessageChannel.TELEGRAM);
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
        StringBuilder text = new StringBuilder(userMessage.getMessage());
        if(userMessage.getPayload() != null && userMessage.getPayload().getChoices() != null) {
            ArrayList choices = new ArrayList();
            userMessage.getPayload().getChoices().forEach(item -> {
                text.append("\n").append(item.getText());
            });
        }
        OutboundMessage outboundMessage = OutboundMessage.builder()
                .text(text.toString())
                .chatId(userMessage.getToSource())
                .build();

        return outboundMessage;
    }

    /**
     * Send outbound message to telegram
     * @param userMessageDto
     * @return
     */
    public Mono<UserMessageDto> sendOutboundMessage(UserMessageDto userMessageDto) {
        OutboundMessage outboundMessage = (OutboundMessage) convertOutboundMsgFromMessageFormat(userMessageDto);

        return (new TelegramOutboundService(outboundBaseUrl)).sendOutboundMessage(outboundMessage)
                .map(new Function<OutboundResponse, UserMessageDto>() {
                    @Override
                    public UserMessageDto apply(OutboundResponse outboundResponse) {
                        if(outboundResponse.getOk()) {
                            userMessageDto.setStatus(UserMessageStatus.SENT);
                        } else {
                            userMessageDto.setStatus(UserMessageStatus.NOT_SENT);
                        }
                        return userMessageDto;
                    }
                });
    }
}

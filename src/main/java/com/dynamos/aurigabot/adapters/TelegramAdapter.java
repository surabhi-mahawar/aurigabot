package com.dynamos.aurigabot.adapters;

import com.dynamos.aurigabot.dto.MessagePayloadDto;
import com.dynamos.aurigabot.dto.UserMessageDto;
import com.dynamos.aurigabot.enums.ChannelProvider;
import com.dynamos.aurigabot.enums.MessageChannel;
import com.dynamos.aurigabot.enums.MessagePayloadType;
import com.dynamos.aurigabot.enums.UserMessageStatus;
import com.dynamos.aurigabot.model.telegram.OutboundMessage;
import com.dynamos.aurigabot.response.telegram.OutboundResponse;
import com.dynamos.aurigabot.service.TelegramService;
import com.dynamos.aurigabot.utils.BotUtil;
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
        OutboundMessage outboundMessage = OutboundMessage.builder()
                .text(userMessage.getMessage())
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

        return (new TelegramService(outboundBaseUrl)).sendOutboundMessage(outboundMessage)
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

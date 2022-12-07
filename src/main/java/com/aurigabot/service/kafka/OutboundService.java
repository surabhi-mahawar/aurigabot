package com.aurigabot.service.kafka;

import com.aurigabot.adapters.AbstractAdapter;
import com.aurigabot.adapters.TelegramAdapter;
import com.aurigabot.dto.UserMessageDto;
import com.aurigabot.enums.ChannelProvider;
import com.aurigabot.repository.UserMessageRepository;
import com.aurigabot.response.HttpApiResponse;
import com.aurigabot.service.message.InboundMessageService;
import com.aurigabot.service.message.OutboundMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
public class OutboundService {
    @Autowired
    private UserMessageRepository userMessageRepository;

    @Value("${telegram.bot.apiUrl}")
    private String telegramApiUrl;

    @Value("${telegram.bot.token}")
    private String botToken;

    @KafkaListener(topics = "${kafka.topic.telegram.outbound.message}", groupId = "${kafka.inbound.consumer.group.id}")
    public void listenOutboundTopic(String message) {
        System.out.println("Received Message: " + message);
        ObjectMapper mapper = new ObjectMapper();
        try {
            Object obj = mapper.readValue(message, Message.class);
            UserMessageDto outUserMessageDto = (UserMessageDto) obj;

            HttpApiResponse response = HttpApiResponse.builder()
                    .status(HttpStatus.OK.value())
                    .path("/inbound/telegram")
                    .build();

            OutboundMessageService outboundMessageService = OutboundMessageService.builder()
                    .adapter(getAdapterByChannel())
                    .userMessageRepository(userMessageRepository)
                    .build();

            outboundMessageService.processOutboundMessage(response, outUserMessageDto).subscribe();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private AbstractAdapter getAdapterByChannel() {
        String outboundUrl = telegramApiUrl+"bot"+botToken;
        return new TelegramAdapter(outboundUrl);
    }
}

package com.aurigabot.service.kafka;

import com.aurigabot.adapters.AbstractAdapter;
import com.aurigabot.adapters.TelegramAdapter;
import com.aurigabot.repository.FlowRepository;
import com.aurigabot.repository.LeaveRequestRepository;
import com.aurigabot.repository.UserMessageRepository;
import com.aurigabot.repository.UserRepository;
import com.aurigabot.response.HttpApiResponse;
import com.aurigabot.service.message.InboundMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
public class InboundService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMessageRepository userMessageRepository;

    @Autowired
    private FlowRepository flowRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Value("${telegram.bot.apiUrl}")
    private String telegramApiUrl;

    @Value("${telegram.bot.token}")
    private String botToken;

    @KafkaListener(topics = "${kafka.topic.telegram.inbound.message}", groupId = "${kafka.consumer.group.id}")
    public void listenTelegramInboundTopic(String message) {
        String outboundUrl = telegramApiUrl+"bot"+botToken;
        System.out.println("Received Message in group foo: " + message);
        ObjectMapper mapper = new ObjectMapper();
        try {
            Object obj = mapper.readValue(message, Message.class);
            AbstractAdapter adapter = new TelegramAdapter(outboundUrl);
            InboundMessageService inboundMessageService = InboundMessageService.builder()
                    .adapter(adapter)
                    .userRepository(userRepository)
                    .flowRepository(flowRepository)
                    .userMessageRepository(userMessageRepository)
                    .leaveRequestRepository(leaveRequestRepository)
                    .build();

            HttpApiResponse response = HttpApiResponse.builder()
                    .status(HttpStatus.OK.value())
                    .path("/inbound/telegram")
                    .build();

            inboundMessageService.processInboundMessage(response, obj).subscribe();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}

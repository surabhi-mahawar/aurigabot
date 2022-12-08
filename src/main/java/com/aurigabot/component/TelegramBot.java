package com.aurigabot.component;

import com.aurigabot.adapters.TelegramAdapter;
import com.aurigabot.repository.FlowRepository;
import com.aurigabot.repository.LeaveRequestRepository;
import com.aurigabot.repository.UserMessageRepository;
import com.aurigabot.repository.UserRepository;
import com.aurigabot.response.HttpApiResponse;
import com.aurigabot.adapters.AbstractAdapter;
import com.aurigabot.service.KafkaProducerService;
import com.aurigabot.service.message.InboundMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Value(value = "${kafka.topic.telegram.inbound.message}")
    private String topic;

    @Value("${telegram.bot.username}")
    private String telegramBotUsername;

    @Value("${telegram.bot.token}")
    private String telegramBotToken;

    public String getBotToken() {
        return telegramBotToken;
    }

    public String getBotUsername() {
        return telegramBotUsername;
    }

    /**
     * On message update recieved from telegram - process this message & send this to kafka inbound topic
     * @param update
     */
    @Override
    public void onUpdateReceived(Update update) {
        // Checking if the update has message and it has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            log.info("Received Telegram Message: "+update.getMessage());
            try {
                ObjectMapper Obj = new ObjectMapper();
                String jsonStr = Obj.writeValueAsString(update.getMessage());
                kafkaProducerService.sendMessage(jsonStr, topic);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
}

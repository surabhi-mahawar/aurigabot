package com.dynamos.aurigabot.component;

import com.dynamos.aurigabot.adapters.AbstractAdapter;
import com.dynamos.aurigabot.adapters.TelegramAdapter;
import com.dynamos.aurigabot.adapters.WebPortalAdapter;
import com.dynamos.aurigabot.repository.FlowRepository;
import com.dynamos.aurigabot.repository.UserMessageRepository;
import com.dynamos.aurigabot.repository.UserRepository;
import com.dynamos.aurigabot.response.HttpApiResponse;
import com.dynamos.aurigabot.service.InboundMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMessageRepository userMessageRepository;

    @Autowired
    private FlowRepository flowRepository;

    String botToken;
    String botUsername;
    String outboundBaseUrl;

    TelegramBot(@Value("${telegram.bot.token}") String botToken,
                @Value("${telegram.bot.username}") String botUsername,
                @Value("${telegram.bot.apiUrl}") String telegramApiUrl) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.outboundBaseUrl = telegramApiUrl+"bot"+botToken;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    public String getOutboundBaseUrl() {
        return outboundBaseUrl;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Checking if the update has message and it has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            System.out.println("Received Telegram Message: "+update.getMessage());

            AbstractAdapter adapter = new TelegramAdapter(getOutboundBaseUrl());
            InboundMessageService inboundMessageService = InboundMessageService.builder()
                    .adapter(adapter)
                    .userRepository(userRepository)
                    .flowRepository(flowRepository)
                    .userMessageRepository(userMessageRepository)
                    .build();

            HttpApiResponse response = HttpApiResponse.builder()
                    .status(HttpStatus.OK.value())
                    .path("/inbound/telegram")
                    .build();

            inboundMessageService
                    .processInboundMessage(response, update.getMessage())
                    .subscribe();

            // Creating object of SendMessage
//            SendMessage message = new SendMessage();
//            // Setting chat id
//            message.setChatId(update.getMessage().getChatId().toString());
//            // Setting reply to message id
//            message.setReplyToMessageId(update.getMessage().getMessageId());
//            // Getting and setting received message text
//            message.setText(update.getMessage().getText());
//            try {
//                // Sending message
//                execute(message);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }
}

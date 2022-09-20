package com.dynamos.aurigabot.service;

import com.dynamos.aurigabot.model.telegram.OutboundMessage;
import com.dynamos.aurigabot.response.telegram.OutboundResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

public class TelegramService {
    private final WebClient webClient;

    public TelegramService(String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * Send outbound message to telegram using web client
     * @param outboundMessage
     * @return
     */
    public Mono<OutboundResponse> sendOutboundMessage(OutboundMessage outboundMessage) {
        return webClient.get()
                .uri(uriBuilder -> {
                    return uriBuilder.path("/sendMessage")
                            .queryParam("chat_id", outboundMessage.getChatId())
                            .queryParam("text", outboundMessage.getText())
                            .build();
                })
                .retrieve()
                .bodyToMono(OutboundResponse.class)
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        System.out.println("ERROR IS " + throwable.getLocalizedMessage());
                    }
                });
    }
}

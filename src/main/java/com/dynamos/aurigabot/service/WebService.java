package com.dynamos.aurigabot.service;

import com.dynamos.aurigabot.model.web.OutboundMessage;
import com.dynamos.aurigabot.model.web.OutboundResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

public class WebService {
    private final WebClient webClient;

    public WebService() {
        this.webClient = WebClient.builder().build();
    }

    public Mono<OutboundResponse> sendOutboundMessage(String url, OutboundMessage outboundMessage) {
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(outboundMessage), OutboundMessage.class)
                .retrieve()
                .bodyToMono(OutboundResponse.class)
                .map(new Function<OutboundResponse, OutboundResponse>() {
                    @Override
                    public OutboundResponse apply(OutboundResponse webResponse) {
                        if (webResponse != null) {
                            System.out.println("MESSAGE RESPONSE " + webResponse.getMessage());
                            System.out.println("STATUS RESPONSE " + webResponse.getStatus());
                            System.out.println("MESSAGE ID RESPONSE " + webResponse.getId());
                            return webResponse;
                        } else {
                            return null;
                        }
                    }
                }).doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        System.out.println("ERROR IS " + throwable.getLocalizedMessage());
                    }
                });
    }
}

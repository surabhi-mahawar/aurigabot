package com.aurigabot.service.outbound;

import com.aurigabot.dto.netcore.whatsapp.outbound.OutboundMessage;
import com.aurigabot.response.netcore.whatsapp.OutboundResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

public class NetcoreWhatsappService {
    private final WebClient webClient;

    public NetcoreWhatsappService(String baseUrl, String token) {
        webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    /**
     * Send outbound message to netcore whatsapp using web client
     * @param outboundMessage
     * @return
     */
    public Mono<OutboundResponse> sendOutboundMessage(OutboundMessage outboundMessage) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            String json = ow.writeValueAsString(outboundMessage);
            System.out.println("json:"+json);
        } catch (JsonProcessingException e) {
            System.out.println("json not converted:"+e.getMessage());
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return webClient.post()
                .uri("/message/")
                .body(Mono.just(outboundMessage), OutboundMessage.class)
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

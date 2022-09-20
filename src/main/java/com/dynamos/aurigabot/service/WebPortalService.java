package com.dynamos.aurigabot.service;

import com.dynamos.aurigabot.model.webPortal.OutboundMessage;
import com.dynamos.aurigabot.response.webPortal.OutboundResponse;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

public class WebPortalService {
    private final WebClient webClient;

    public WebPortalService() {
        this.webClient = WebClient.builder().build();
    }

    /**
     * Send outbound message to web portal using web client
     * @param url
     * @param outboundMessage
     * @return
     */
    public Mono<OutboundResponse> sendOutboundMessage(String url, OutboundMessage outboundMessage) {
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(outboundMessage), OutboundMessage.class)
                .retrieve()
                .bodyToMono(OutboundResponse.class)
//                .map(new Function<OutboundResponse, OutboundResponse>() {
//                    @Override
//                    public OutboundResponse apply(OutboundResponse webResponse) {
//                        if (webResponse != null) {
//                            System.out.println("MESSAGE RESPONSE " + webResponse.getMessage());
//                            System.out.println("STATUS RESPONSE " + webResponse.getStatus());
//                            System.out.println("MESSAGE ID RESPONSE " + webResponse.getId());
//                            return webResponse;
//                        } else {
//                            return null;
//                        }
//                    }
//                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        System.out.println("ERROR IS " + throwable.getLocalizedMessage());
                    }
                });
    }
}

package com.aurigabot.controller.inbound;

import com.aurigabot.adapters.AbstractAdapter;
import com.aurigabot.adapters.WebPortalAdapter;
import com.aurigabot.model.webPortal.InboundMessage;
import com.aurigabot.repository.FlowRepository;
import com.aurigabot.repository.LeaveRequestRepository;
import com.aurigabot.repository.UserMessageRepository;
import com.aurigabot.repository.UserRepository;
import com.aurigabot.response.HttpApiResponse;
import com.aurigabot.service.KafkaProducerService;
import com.aurigabot.service.message.InboundMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(value = "/inbound")
public class WebPortalController {
    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Value(value = "${kafka.topic.webPortal.inbound.message}")
    private String topic;

    @Value("${web.portal.url}")
    public String outboundUrl;

    /**
     * Receives inbound message from web channel and process it.
     * @param message
     */
    @PostMapping(value = "/webMessage", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<HttpApiResponse> webMessage(@RequestBody InboundMessage message) {
        log.info("Received Web Portal Message: "+message);
        try {
            ObjectMapper Obj = new ObjectMapper();
            String jsonStr = Obj.writeValueAsString(message);
            kafkaProducerService.sendMessage(jsonStr, topic);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        HttpApiResponse response = HttpApiResponse.builder()
                .status(HttpStatus.OK.value())
                .path("/inbound/webMessage")
                .build();

        return Mono.just(response);
    }
}

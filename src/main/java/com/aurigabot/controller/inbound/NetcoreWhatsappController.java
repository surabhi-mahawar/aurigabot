package com.aurigabot.controller.inbound;

import com.aurigabot.dto.netcore.whatsapp.inbound.Message;
import com.aurigabot.response.HttpApiResponse;
import com.aurigabot.service.KafkaProducerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(value = "/inbound/netcore/whatsapp")
public class NetcoreWhatsappController {
    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Value(value = "${kafka.topic.netcore.whatsapp.inbound.message}")
    private String topic;

    /**
     * Receives inbound message from web channel and process it.
     * @param message
     */
    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<HttpApiResponse> webMessage(@RequestBody Message message) {
        log.info("Received Netcore Whataspp Message: "+message);
        try {
            ObjectMapper Obj = new ObjectMapper();
            String jsonStr = Obj.writeValueAsString(message);
            kafkaProducerService.sendMessage(jsonStr, topic);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        HttpApiResponse response = HttpApiResponse.builder()
                .status(HttpStatus.OK.value())
                .path("/inbound/netcore/whatsapp/")
                .build();

        return Mono.just(response);
    }
}

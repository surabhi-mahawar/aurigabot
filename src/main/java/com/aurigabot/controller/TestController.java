package com.aurigabot.controller;

import com.aurigabot.service.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Value(value = "${kafka.topic.inbound.message}")
    private String topic;

    @GetMapping("/test-kafka")
    private void testKafka() {
        kafkaProducerService.sendMessage("Test-message", topic);
    }
}

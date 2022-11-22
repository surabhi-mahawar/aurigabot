package com.aurigabot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    @Value(value = "${kafka.consumer.group.id}")
    private String groupId;

    @Value(value = "${kafka.topic.inbound.message}")
    private String topic;

    @KafkaListener(topics = "${kafka.topic.inbound.message}", groupId = "${kafka.consumer.group.id}")
    public void listenGroupFoo(String message) {

        System.out.println("Received Message in group foo: " + message);

    }
}

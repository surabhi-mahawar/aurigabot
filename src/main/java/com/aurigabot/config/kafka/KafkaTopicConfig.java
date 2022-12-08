package com.aurigabot.config.kafka;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value = "${kafka.topic.telegram.inbound.message}")
    private String telegramTopic;

    @Value(value = "${kafka.topic.webPortal.inbound.message}")
    private String webPortalTopic;

    @Value(value = "${kafka.topic.user.message}")
    private String messageTopic;

    @Value(value = "${kafka.topic.outbound.message}")
    private String outboundTopic;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic telegramInboundMessageTopic() {
        return new NewTopic(telegramTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic webPortalInboundMessageTopic() {
        return new NewTopic(webPortalTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic userMessageTopic() {
        return new NewTopic(messageTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic outboundMessageTopic() {
        return new NewTopic(outboundTopic, 1, (short) 1);
    }
}

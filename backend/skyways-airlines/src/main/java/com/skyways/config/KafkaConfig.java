package com.skyways.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic bookingCreatedTopic() {
        return TopicBuilder.name("booking-created")
            .partitions(1)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic paymentProcessedTopic() {
        return TopicBuilder.name("payment-processed")
            .partitions(1)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic notificationSendTopic() {
        return TopicBuilder.name("notification-send")
            .partitions(1)
            .replicas(1)
            .build();
    }
}
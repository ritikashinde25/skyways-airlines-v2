package com.skyways.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    private static final Logger logger =
        LoggerFactory.getLogger(PaymentEventProducer.class);

    private static final String TOPIC = "payment-processed";

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendPaymentEvent(String message) {
        if (kafkaTemplate != null) {
            logger.info("Publishing payment event: {}", message);
            kafkaTemplate.send(TOPIC, message);
            logger.info("Payment event published successfully");
        } else {
            logger.warn("Kafka not available, skipping event: {}", 
                message);
        }
    }
}
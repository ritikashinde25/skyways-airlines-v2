package com.skyways.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BookingEventProducer {

    private static final Logger logger =
        LoggerFactory.getLogger(BookingEventProducer.class);

    private static final String TOPIC = "booking-created";

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendBookingEvent(String message) {
        if (kafkaTemplate != null) {
            try {
                logger.info("Publishing booking event: {}", 
                    message);
                kafkaTemplate.send(TOPIC, message);
                logger.info("Booking event published successfully");
            } catch (Exception e) {
                logger.warn("Kafka not available, " +
                    "skipping booking event: {}", 
                    e.getMessage());
            }
        } else {
            logger.warn("Kafka not available, " +
                "skipping event: {}", message);
        }
    }
}
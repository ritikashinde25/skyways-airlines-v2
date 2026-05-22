package com.skyways.kafka;

import com.skyways.entity.Notification;
import com.skyways.enums.NotificationStatus;
import com.skyways.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class NotificationEventConsumer {

    private static final Logger logger =
        LoggerFactory.getLogger(NotificationEventConsumer.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @KafkaListener(topics = "payment-processed",
                   groupId = "notification-group")
    public void consumePaymentEvent(String message) {
        logger.info("Received payment event: {}", message);
        try {
            String[] parts = message.split("\\|");
            if (parts.length >= 5) {
                String username = parts[2];
                String bookingId = parts[1];

                Notification notification = Notification.builder()
                    .username(username)
                    .email(username + "@skyways.com")
                    .type("BOOKING_CONFIRMATION")
                    .subject("Booking Confirmed - SkyWays Airlines")
                    .message("Dear " + username +
                        ", your booking " + bookingId +
                        " has been confirmed. " +
                        "Thank you for choosing SkyWays Airlines!")
                    .status(NotificationStatus.SENT)
                    .sentAt(LocalDateTime.now().toString())
                    .build();

                notificationRepository.save(notification);
                logger.info("Notification saved for user: {}",
                    username);
            }
        } catch (Exception e) {
            logger.error("Error processing payment event: {}",
                e.getMessage());
        }
    }

    @KafkaListener(topics = "booking-created",
                   groupId = "booking-group")
    public void consumeBookingEvent(String message) {
        logger.info("Received booking event: {}", message);
    }
}
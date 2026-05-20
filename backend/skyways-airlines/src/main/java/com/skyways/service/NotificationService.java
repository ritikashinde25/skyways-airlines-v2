package com.skyways.service;

import com.skyways.constants.AppConstants;
import com.skyways.entity.Notification;
import com.skyways.enums.NotificationStatus;
import com.skyways.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger logger =
        LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public Notification sendNotification(Notification notification) {
        logger.info("Sending notification to: {}", 
            notification.getUsername());
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now().toString());
        Notification saved = notificationRepository.save(notification);
        logger.info("Notification sent with ID: {}", saved.getId());
        return saved;
    }

    public Notification sendBookingConfirmation(String username,
            String email, String flightNumber,
            String origin, String destination) {
        logger.info("Sending booking confirmation to: {}", username);

        Notification notification = Notification.builder()
                .username(username)
                .email(email)
                .type("BOOKING_CONFIRMATION")
                .subject("Booking Confirmed - SkyWays Airlines")
                .message("Dear " + username + 
                    ", your booking for flight " + flightNumber +
                    " from " + origin + " to " + destination +
                    " is confirmed. Thank you for choosing SkyWays!")
                .status(NotificationStatus.SENT)
                .sentAt(LocalDateTime.now().toString())
                .build();

        return notificationRepository.save(notification);
    }

    public List<Notification> getAllNotifications() {
        logger.info("Fetching all notifications");
        return notificationRepository.findAll();
    }

    public List<Notification> getNotificationsByUsername(
            String username) {
        logger.info("Fetching notifications for: {}", username);
        return notificationRepository.findByUsername(username);
    }
}
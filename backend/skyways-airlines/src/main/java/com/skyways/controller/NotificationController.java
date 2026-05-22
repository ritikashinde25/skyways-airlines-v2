package com.skyways.controller;

import com.skyways.entity.Notification;
import com.skyways.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    private static final Logger logger =
            LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok(
            "SkyWays Notification Service is running!");
    }

    @PostMapping("/send")
    public ResponseEntity<Notification> sendNotification(
            @RequestBody Notification notification) {
        logger.info("Send notification to: {}",
            notification.getUsername());
        Notification sent = notificationService
            .sendNotification(notification);
        return ResponseEntity.status(HttpStatus.CREATED).body(sent);
    }

    @PostMapping("/booking-confirmation")
    public ResponseEntity<Notification> sendBookingConfirmation(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String flightNumber,
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String flightDate,
            @RequestParam String departureTime,
            @RequestParam String arrivalTime,
            @RequestParam String seatNumber,
            @RequestParam String travelClass,
            @RequestParam String pnr) {
        Notification sent = notificationService.sendBookingConfirmation(
                username, email, flightNumber, origin, destination,
                flightDate, departureTime, arrivalTime,
                seatNumber, travelClass, pnr);
        return ResponseEntity.status(HttpStatus.CREATED).body(sent);
    }

    @PostMapping("/cancellation")
    public ResponseEntity<Notification> sendCancellation(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String flightNumber,
            @RequestParam String pnr) {
        Notification sent = notificationService.sendCancellationNotice(
                username, email, flightNumber, pnr);
        return ResponseEntity.status(HttpStatus.CREATED).body(sent);
    }

    @PostMapping("/flight-delay")
    public ResponseEntity<Notification> sendFlightDelay(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String flightNumber,
            @RequestParam String newDepartureTime) {
        Notification sent = notificationService.sendFlightDelayAlert(
                username, email, flightNumber, newDepartureTime);
        return ResponseEntity.status(HttpStatus.CREATED).body(sent);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(
            notificationService.getAllNotifications());
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<Notification>> getByUsername(
            @PathVariable String username) {
        return ResponseEntity.ok(
            notificationService.getNotificationsByUsername(username));
    }
}
package com.skyways.controller;

import com.skyways.dto.BookingDTO;
import com.skyways.entity.Booking;
import com.skyways.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class BookingController {

    private static final Logger logger =
        LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok(
            "SkyWays Booking Service is running!");
    }

    @PostMapping("/create")
    public ResponseEntity<Booking> createBooking(
            @Valid @RequestBody BookingDTO bookingDTO) {
        logger.info("Create booking for: {}", 
            bookingDTO.getUsername());
        Booking booking = bookingService.createBooking(bookingDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(booking);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<Booking>> getBookingsByUsername(
            @PathVariable String username) {
        logger.info("Get bookings for user: {}", username);
        return ResponseEntity.ok(
            bookingService.getBookingsByUsername(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(
            @PathVariable Long id) {
        logger.info("Get booking by ID: {}", id);
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<Map<String, Object>> cancelBooking(
            @PathVariable Long id) {
        logger.info("Cancel booking: {}", id);
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }
}
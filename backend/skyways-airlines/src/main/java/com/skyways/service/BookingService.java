package com.skyways.service;

import com.skyways.constants.AppConstants;
import com.skyways.dto.BookingDTO;
import com.skyways.entity.Booking;
import com.skyways.enums.BookingStatus;
import com.skyways.enums.FlightClass;
import com.skyways.enums.SeatType;
import com.skyways.exception.ResourceNotFoundException;
import com.skyways.mapper.BookingMapper;
import com.skyways.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Logger logger =
        LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    public Booking createBooking(BookingDTO bookingDTO) {
        logger.info("Creating booking for user: {}",
            bookingDTO.getUsername());

        Booking booking = Booking.builder()
                .username(bookingDTO.getUsername())
                .flightId(bookingDTO.getFlightId())
                .flightNumber(bookingDTO.getFlightNumber())
                .origin(bookingDTO.getOrigin())
                .destination(bookingDTO.getDestination())
                .bookingDate(bookingDTO.getBookingDate())
                .totalPrice(bookingDTO.getTotalPrice())
                .status(BookingStatus.CONFIRMED)
                .travelClass(bookingDTO.getTravelClass() != null ?
                    FlightClass.valueOf(bookingDTO.getTravelClass()
                        .replace(" ", "_").toUpperCase()) :
                    FlightClass.ECONOMY)
                .seatType(bookingDTO.getSeatType() != null ?
                    SeatType.valueOf(bookingDTO.getSeatType()
                        .toUpperCase()) : SeatType.MIDDLE)
                .build();

        Booking saved = bookingRepository.save(booking);
        logger.info("Booking created with ID: {}", saved.getId());
        return saved;
    }

    public List<Booking> getAllBookings() {
        logger.info("Fetching all bookings");
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByUsername(String username) {
        logger.info("Fetching bookings for user: {}", username);
        return bookingRepository.findByUsername(username);
    }

    public Booking getBookingById(Long id) {
        logger.info("Fetching booking with ID: {}", id);
        return bookingRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("Booking not found: {}", id);
                return new ResourceNotFoundException(
                    AppConstants.ERROR_BOOKING_NOT_FOUND + ": " + id);
            });
    }

    public Map<String, Object> cancelBooking(Long id) {
        logger.info("Cancelling booking: {}", id);
        Booking booking = getBookingById(id);

        // Calculate refund based on travel date
        LocalDate travelDate = LocalDate.parse(
            booking.getBookingDate());
        LocalDate today = LocalDate.now();
        long daysUntilTravel = ChronoUnit.DAYS.between(
            today, travelDate);

        double refundPercentage;
        String refundMessage;

        if (daysUntilTravel >= 7) {
            refundPercentage = 100.0;
            refundMessage = "Full refund of 100%";
        } else if (daysUntilTravel >= 3) {
            refundPercentage = 75.0;
            refundMessage = "Partial refund of 75%";
        } else if (daysUntilTravel >= 1) {
            refundPercentage = 50.0;
            refundMessage = "Partial refund of 50%";
        } else {
            refundPercentage = 0.0;
            refundMessage = "No refund for same day cancellation";
        }

        double refundAmount = booking.getTotalPrice() *
            refundPercentage / 100;
        double deductionAmount = booking.getTotalPrice() - 
            refundAmount;

        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        logger.info("Booking cancelled: {}, Refund: {}%",
            id, refundPercentage);

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("message", 
            "Booking cancelled successfully!");
        response.put("bookingId", id);
        response.put("totalPaid", booking.getTotalPrice());
        response.put("refundPercentage", refundPercentage);
        response.put("refundAmount", refundAmount);
        response.put("deductionAmount", deductionAmount);
        response.put("refundMessage", refundMessage);
        response.put("daysUntilTravel", daysUntilTravel);

        return response;
    }

    public BookingDTO getBookingDTOById(Long id) {
        logger.info("Fetching booking DTO with ID: {}", id);
        Booking booking = getBookingById(id);
        return bookingMapper.toDTO(booking);
    }
}
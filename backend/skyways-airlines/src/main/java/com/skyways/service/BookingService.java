package com.skyways.service;

import com.skyways.constants.AppConstants;
import com.skyways.dto.BookingDTO;
import com.skyways.entity.Booking;
import com.skyways.enums.BookingStatus;
import com.skyways.enums.FlightClass;
import com.skyways.enums.SeatType;
import com.skyways.exception.ResourceNotFoundException;
import com.skyways.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Logger logger =
        LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;

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

    public String cancelBooking(Long id) {
        logger.info("Cancelling booking: {}", id);
        Booking booking = getBookingById(id);
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        logger.info("Booking cancelled: {}", id);
        return AppConstants.SUCCESS_CANCEL;
    }
}
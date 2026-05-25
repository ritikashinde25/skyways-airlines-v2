package com.skyways.service;

import com.skyways.constants.AppConstants;
import com.skyways.dto.BookingDTO;
import com.skyways.entity.Booking;
import com.skyways.entity.Payment;
import com.skyways.enums.BookingStatus;
import com.skyways.enums.FlightClass;
import com.skyways.enums.SeatType;
import com.skyways.exception.ResourceNotFoundException;
import com.skyways.kafka.BookingEventProducer;
import com.skyways.mapper.BookingMapper;
import com.skyways.repository.BookingRepository;
import com.skyways.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    // Real airplane seat distribution
    private static final int MAX_SEATS_PER_FLIGHT = 180;
    private static final int MAX_WINDOW_SEATS = 60;
    private static final int MAX_AISLE_SEATS = 60;
    private static final int MAX_MIDDLE_SEATS = 60;

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;
    private final BookingEventProducer bookingEventProducer;

    @Transactional
    public Booking createBooking(BookingDTO bookingDTO) {
        logger.info("Creating booking for user: {}",
            bookingDTO.getUsername());

        synchronized (bookingDTO.getFlightId().intern()) {

            SeatType seatType = bookingDTO.getSeatType() != null ?
                SeatType.valueOf(bookingDTO.getSeatType()
                    .toUpperCase()) : SeatType.MIDDLE;

            // Check if specific seat type is full
            long seatTypeCount = bookingRepository
                .countByFlightIdAndSeatTypeAndStatus(
                    bookingDTO.getFlightId(),
                    seatType,
                    BookingStatus.CONFIRMED);

            int maxSeatsForType = getMaxSeatsForType(seatType);

            if (seatTypeCount >= maxSeatsForType) {
                logger.warn("{} seats fully booked for flight: {}",
                    seatType, bookingDTO.getFlightId());
                throw new ResourceNotFoundException(
                    seatType + " seats are not available! " +
                    "Please select a different seat type.");
            }

            // Check if flight is fully booked
            long totalBookings = bookingRepository
                .countByFlightIdAndStatus(
                    bookingDTO.getFlightId(),
                    BookingStatus.CONFIRMED);

            if (totalBookings >= MAX_SEATS_PER_FLIGHT) {
                logger.warn("Flight fully booked: {}",
                    bookingDTO.getFlightId());
                throw new ResourceNotFoundException(
                    "Flight is fully booked!");
            }

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
                    .seatType(seatType)
                    .build();

            Booking saved = bookingRepository.save(booking);
            logger.info("Booking created with ID: {}",
                saved.getId());

            // Publish Kafka event
            String event = "BOOKING_CREATED|" + saved.getId() +
                "|" + saved.getUsername() +
                "|" + saved.getFlightNumber() +
                "|" + saved.getOrigin() +
                "|" + saved.getDestination() +
                "|" + saved.getTotalPrice();
            bookingEventProducer.sendBookingEvent(event);

            return saved;
        }
    }

    private int getMaxSeatsForType(SeatType seatType) {
        return switch (seatType) {
            case WINDOW -> MAX_WINDOW_SEATS;
            case AISLE -> MAX_AISLE_SEATS;
            case MIDDLE -> MAX_MIDDLE_SEATS;
        };
    }

    public Map<String, Object> getFlightAvailability(
            String flightId) {
        logger.info("Checking availability for flight: {}",
            flightId);

        long totalBookings = bookingRepository
            .countByFlightIdAndStatus(
                flightId, BookingStatus.CONFIRMED);

        long windowBooked = bookingRepository
            .countByFlightIdAndSeatTypeAndStatus(
                flightId, SeatType.WINDOW,
                BookingStatus.CONFIRMED);

        long aisleBooked = bookingRepository
            .countByFlightIdAndSeatTypeAndStatus(
                flightId, SeatType.AISLE,
                BookingStatus.CONFIRMED);

        long middleBooked = bookingRepository
            .countByFlightIdAndSeatTypeAndStatus(
                flightId, SeatType.MIDDLE,
                BookingStatus.CONFIRMED);

        long windowAvailable = MAX_WINDOW_SEATS - windowBooked;
        long aisleAvailable = MAX_AISLE_SEATS - aisleBooked;
        long middleAvailable = MAX_MIDDLE_SEATS - middleBooked;
        long totalAvailable = MAX_SEATS_PER_FLIGHT - totalBookings;

        Map<String, Object> availability = new HashMap<>();
        availability.put("flightId", flightId);
        availability.put("totalSeats", MAX_SEATS_PER_FLIGHT);
        availability.put("totalBooked", totalBookings);
        availability.put("availableSeats", totalAvailable);
        availability.put("isFullyBooked", totalAvailable <= 0);

        // Window seats
        availability.put("windowTotal", MAX_WINDOW_SEATS);
        availability.put("windowBooked", windowBooked);
        availability.put("windowAvailable", windowAvailable);
        availability.put("windowFull", windowAvailable <= 0);

        // Aisle seats
        availability.put("aisleTotal", MAX_AISLE_SEATS);
        availability.put("aisleBooked", aisleBooked);
        availability.put("aisleAvailable", aisleAvailable);
        availability.put("aisleFull", aisleAvailable <= 0);

        // Middle seats
        availability.put("middleTotal", MAX_MIDDLE_SEATS);
        availability.put("middleBooked", middleBooked);
        availability.put("middleAvailable", middleAvailable);
        availability.put("middleFull", middleAvailable <= 0);

        return availability;
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

        boolean stripeRefundProcessed = false;
        if (refundAmount > 0) {
            List<Payment> payments = paymentRepository
                .findByBookingId(booking.getId());
            if (!payments.isEmpty()) {
                Payment payment = payments.get(0);
                if (payment.getStripePaymentIntentId() != null) {
                    Map<String, Object> stripeResponse =
                        stripeService.refundPayment(
                            payment.getStripePaymentIntentId(),
                            (long) refundAmount);
                    stripeRefundProcessed =
                        (boolean) stripeResponse
                            .getOrDefault("success", false);
                    logger.info("Stripe refund processed: {}",
                        stripeRefundProcessed);
                }
            }
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        logger.info("Booking cancelled: {}, Refund: {}%",
            id, refundPercentage);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Booking cancelled successfully!");
        response.put("bookingId", id);
        response.put("totalPaid", booking.getTotalPrice());
        response.put("refundPercentage", refundPercentage);
        response.put("refundAmount", refundAmount);
        response.put("deductionAmount", deductionAmount);
        response.put("refundMessage", refundMessage);
        response.put("daysUntilTravel", daysUntilTravel);
        response.put("stripeRefundProcessed",
            stripeRefundProcessed);

        return response;
    }

    public BookingDTO getBookingDTOById(Long id) {
        logger.info("Fetching booking DTO with ID: {}", id);
        Booking booking = getBookingById(id);
        return bookingMapper.toDTO(booking);
    }
}
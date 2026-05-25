package com.skyways.repository;

import com.skyways.entity.Booking;
import com.skyways.enums.BookingStatus;
import com.skyways.enums.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    List<Booking> findByUsername(String username);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByFlightId(String flightId);

    long countByFlightIdAndStatus(
        String flightId, BookingStatus status);

    long countByFlightIdAndSeatTypeAndStatus(
        String flightId, SeatType seatType,
        BookingStatus status);
}
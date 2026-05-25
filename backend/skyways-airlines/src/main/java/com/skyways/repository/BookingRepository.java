package com.skyways.repository;

import com.skyways.entity.Booking;
import com.skyways.enums.BookingStatus;
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

    boolean existsByFlightIdAndSeatTypeAndStatus(
        String flightId, 
        com.skyways.enums.SeatType seatType,
        BookingStatus status);
}
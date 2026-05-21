package com.skyways.service;

import com.skyways.entity.Flight;
import com.skyways.exception.ResourceNotFoundException;
import com.skyways.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightService {

    private static final Logger logger =
        LoggerFactory.getLogger(FlightService.class);

    private final FlightRepository flightRepository;

    public List<Flight> getAllFlights() {
        logger.info("Fetching all flights");
        return flightRepository.findAll();
    }

    public List<Flight> searchFlights(String origin,
            String destination) {
        logger.info("Searching flights from {} to {}",
            origin, destination);
        return flightRepository
            .findByOriginIgnoreCaseAndDestinationIgnoreCase(
                origin, destination);
    }

    public Flight getFlightById(String id) {
        logger.info("Fetching flight with ID: {}", id);
        return flightRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("Flight not found: {}", id);
                return new ResourceNotFoundException(
                    "Flight not found: " + id);
            });
    }
}
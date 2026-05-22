package com.skyways.controller;

import com.skyways.entity.Flight;
import com.skyways.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class FlightController {

    private static final Logger logger =
        LoggerFactory.getLogger(FlightController.class);

    private final FlightService flightService;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok(
            "SkyWays Flight Service is running!");
    }

    @GetMapping("/all")
    public ResponseEntity<List<Flight>> getAllFlights() {
        logger.info("Get all flights request");
        return ResponseEntity.ok(flightService.getAllFlights());
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchFlights(
            @RequestParam String origin,
            @RequestParam String destination) {
        logger.info("Search flights: {} to {}", origin, destination);
        List<Flight> flights = flightService
            .searchFlights(origin, destination);
        return flights.isEmpty()
            ? ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("No flights found from " 
                    + origin + " to " + destination)
            : ResponseEntity.ok(flights);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Flight> getFlightById(
            @PathVariable String id) {
        logger.info("Get flight by ID: {}", id);
        return ResponseEntity.ok(flightService.getFlightById(id));
    }
}
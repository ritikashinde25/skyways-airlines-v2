package com.skyways.controller;

import com.skyways.service.SkyscannerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skyscanner")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class SkyscannerController {

    private static final Logger logger =
        LoggerFactory.getLogger(SkyscannerController.class);

    private final SkyscannerService skyscannerService;

    // 1. Search Flights
    @GetMapping("/search")
    public ResponseEntity<Object> searchFlights(
            @RequestParam String originSkyId,
            @RequestParam String destinationSkyId,
            @RequestParam String originEntityId,
            @RequestParam String destinationEntityId,
            @RequestParam String date,
            @RequestParam(required = false) String returnDate,
            @RequestParam(defaultValue = "economy") String cabinClass,
            @RequestParam(defaultValue = "1") int adults,
            @RequestParam(defaultValue = "0") int childrens,
            @RequestParam(defaultValue = "0") int infants) {

        logger.info("Search flights: {} to {}", 
            originSkyId, destinationSkyId);
        return ResponseEntity.ok(
            skyscannerService.searchFlights(
                originSkyId, destinationSkyId,
                originEntityId, destinationEntityId,
                date, returnDate, cabinClass,
                adults, childrens, infants));
    }

    // 2. Search Everywhere
    @GetMapping("/search-everywhere")
    public ResponseEntity<Object> searchEverywhere(
            @RequestParam String originSkyId,
            @RequestParam String originEntityId,
            @RequestParam(defaultValue = "1") int adults,
            @RequestParam(defaultValue = "economy") 
                String cabinClass) {

        logger.info("Search everywhere from: {}", originSkyId);
        return ResponseEntity.ok(
            skyscannerService.searchFlightEverywhere(
                originSkyId, originEntityId,
                adults, cabinClass));
    }

    // 3. Search Incomplete
    @GetMapping("/search-incomplete")
    public ResponseEntity<Object> searchIncomplete(
            @RequestParam String sessionId,
            @RequestParam(defaultValue = "INR") String currency) {

        logger.info("Search incomplete: {}", sessionId);
        return ResponseEntity.ok(
            skyscannerService.searchIncomplete(
                sessionId, currency));
    }

    // 4. Get Flight Details
    @GetMapping("/flight-details")
    public ResponseEntity<Object> getFlightDetails(
            @RequestParam String sessionId,
            @RequestParam String itineraryId,
            @RequestParam(defaultValue = "INR") String currency) {

        logger.info("Get flight details: {}", itineraryId);
        return ResponseEntity.ok(
            skyscannerService.getFlightDetails(
                sessionId, itineraryId, currency));
    }

    // 5. Cheapest Oneway
    @GetMapping("/cheapest-oneway")
    public ResponseEntity<Object> getCheapestOneway(
            @RequestParam String originSkyId,
            @RequestParam String destinationSkyId,
            @RequestParam String month) {

        logger.info("Cheapest oneway: {} to {}", 
            originSkyId, destinationSkyId);
        return ResponseEntity.ok(
            skyscannerService.getCheapestOneway(
                originSkyId, destinationSkyId, month));
    }

    // 6. Price Calendar
    @GetMapping("/price-calendar")
    public ResponseEntity<Object> getPriceCalendar(
            @RequestParam String originSkyId,
            @RequestParam String destinationSkyId,
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam(defaultValue = "economy") 
                String cabinClass) {

        logger.info("Price calendar: {} to {}", 
            originSkyId, destinationSkyId);
        return ResponseEntity.ok(
            skyscannerService.getPriceCalendar(
                originSkyId, destinationSkyId,
                fromDate, toDate, cabinClass));
    }

    // 7. Price Calendar Return
    @GetMapping("/price-calendar-return")
    public ResponseEntity<Object> getPriceCalendarReturn(
            @RequestParam String originSkyId,
            @RequestParam String destinationSkyId,
            @RequestParam String fromDate,
            @RequestParam String returnFromDate) {

        logger.info("Price calendar return: {} to {}",
            originSkyId, destinationSkyId);
        return ResponseEntity.ok(
            skyscannerService.getPriceCalendarReturn(
                originSkyId, destinationSkyId,
                fromDate, returnFromDate));
    }

    // 8. Search Airport
    @GetMapping("/search-airport")
    public ResponseEntity<Object> searchAirport(
            @RequestParam String query) {

        logger.info("Search airport: {}", query);
        return ResponseEntity.ok(
            skyscannerService.searchAirport(query));
    }

    // 9. Multi Stops
    @GetMapping("/multi-stops")
    public ResponseEntity<Object> searchMultiStops(
            @RequestParam String legs,
            @RequestParam(defaultValue = "1") int adults,
            @RequestParam(defaultValue = "economy") 
                String cabinClass) {

        logger.info("Multi-stop search");
        return ResponseEntity.ok(
            skyscannerService.searchFlightsMultiStops(
                legs, adults, cabinClass));
    }
}
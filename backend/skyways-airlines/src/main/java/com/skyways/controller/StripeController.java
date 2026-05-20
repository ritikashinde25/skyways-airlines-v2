package com.skyways.controller;

import com.skyways.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class StripeController {

    private static final Logger logger =
        LoggerFactory.getLogger(StripeController.class);

    private final StripeService stripeService;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, Object>> 
            createPaymentIntent(
            @RequestParam Long amount,
            @RequestParam(defaultValue = "inr") 
                String currency,
            @RequestParam(defaultValue = "SkyWays Flight Booking") 
                String description) {

        logger.info("Create payment intent: {} {}", 
            amount, currency);
        return ResponseEntity.ok(
            stripeService.createPaymentIntent(
                amount, currency, description));
    }

    @GetMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<Map<String, Object>> confirmPayment(
            @PathVariable String paymentIntentId) {

        logger.info("Confirm payment: {}", paymentIntentId);
        return ResponseEntity.ok(
            stripeService.confirmPayment(paymentIntentId));
    }
}
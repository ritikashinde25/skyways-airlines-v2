package com.skyways.controller;

import com.skyways.dto.PaymentDTO;
import com.skyways.entity.Payment;
import com.skyways.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentController {

    private static final Logger logger =
        LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok(
            "SkyWays Payment Service is running!");
    }

    @PostMapping("/process")
    public ResponseEntity<Payment> processPayment(
            @Valid @RequestBody PaymentDTO paymentDTO) {
        logger.info("Process payment for booking: {}", 
            paymentDTO.getBookingId());
        Payment payment = paymentService.processPayment(paymentDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(payment);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<Payment>> getPaymentsByUsername(
            @PathVariable String username) {
        return ResponseEntity.ok(
            paymentService.getPaymentsByUsername(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(
            @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @PutMapping("/refund/{id}")
    public ResponseEntity<String> refundPayment(
            @PathVariable Long id) {
        logger.info("Refund payment: {}", id);
        return ResponseEntity.ok(paymentService.refundPayment(id));
    }
}
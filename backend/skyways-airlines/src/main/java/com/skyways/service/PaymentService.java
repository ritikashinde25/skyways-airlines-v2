package com.skyways.service;

import com.skyways.constants.AppConstants;
import com.skyways.dto.PaymentDTO;
import com.skyways.entity.Payment;
import com.skyways.enums.PaymentStatus;
import com.skyways.exception.ResourceNotFoundException;
import com.skyways.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger logger =
        LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;

    public Payment processPayment(PaymentDTO paymentDTO) {
        logger.info("Processing payment for booking: {}", 
            paymentDTO.getBookingId());

        Payment payment = Payment.builder()
                .bookingId(paymentDTO.getBookingId())
                .username(paymentDTO.getUsername())
                .amount(paymentDTO.getAmount())
                .paymentMethod(paymentDTO.getPaymentMethod())
                .status(PaymentStatus.SUCCESS)
                .transactionId(UUID.randomUUID().toString())
                .paymentDate(LocalDate.now().toString())
                .build();

        Payment saved = paymentRepository.save(payment);
        logger.info("Payment processed with ID: {}", saved.getId());
        return saved;
    }

    public List<Payment> getAllPayments() {
        logger.info("Fetching all payments");
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsByUsername(String username) {
        logger.info("Fetching payments for user: {}", username);
        return paymentRepository.findByUsername(username);
    }

    public Payment getPaymentById(Long id) {
        logger.info("Fetching payment with ID: {}", id);
        return paymentRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("Payment not found: {}", id);
                return new ResourceNotFoundException(
                    AppConstants.ERROR_PAYMENT_NOT_FOUND + ": " + id);
            });
    }

    public String refundPayment(Long id) {
        logger.info("Processing refund for payment: {}", id);
        Payment payment = getPaymentById(id);
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        logger.info("Payment refunded: {}", id);
        return AppConstants.SUCCESS_REFUND;
    }
}
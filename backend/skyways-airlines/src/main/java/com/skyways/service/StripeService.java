package com.skyways.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StripeService {

    private static final Logger logger =
        LoggerFactory.getLogger(StripeService.class);

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    public Map<String, Object> createPaymentIntent(
            Long amount, String currency,
            String description) {

        logger.info("Creating Stripe payment intent: {} {}",
            amount, currency);

        Stripe.apiKey = stripeApiKey;
        Map<String, Object> response = new HashMap<>();

        try {
            PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                    .setAmount(amount * 100)
                    .setCurrency(currency.toLowerCase())
                    .setDescription(description)
                    .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams
                            .AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .build()
                    )
                    .build();

            PaymentIntent paymentIntent =
                PaymentIntent.create(params);

            logger.info("Payment intent created: {}",
                paymentIntent.getId());

            response.put("success", true);
            response.put("clientSecret",
                paymentIntent.getClientSecret());
            response.put("paymentIntentId",
                paymentIntent.getId());
            response.put("amount", amount);
            response.put("currency", currency);
            response.put("status", paymentIntent.getStatus());

        } catch (StripeException e) {
            logger.error("Stripe error: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    public Map<String, Object> confirmPayment(
            String paymentIntentId) {

        logger.info("Confirming payment: {}", paymentIntentId);

        Stripe.apiKey = stripeApiKey;
        Map<String, Object> response = new HashMap<>();

        try {
            PaymentIntent paymentIntent =
                PaymentIntent.retrieve(paymentIntentId);

            response.put("success", true);
            response.put("status", paymentIntent.getStatus());
            response.put("paymentIntentId", paymentIntentId);
            response.put("amount",
                paymentIntent.getAmount() / 100);

            logger.info("Payment status: {}",
                paymentIntent.getStatus());

        } catch (StripeException e) {
            logger.error("Stripe confirm error: {}",
                e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    public Map<String, Object> refundPayment(
            String paymentIntentId, Long amount) {

        logger.info("Processing Stripe refund for: {}",
            paymentIntentId);

        Stripe.apiKey = stripeApiKey;
        Map<String, Object> response = new HashMap<>();

        try {
            RefundCreateParams params =
                RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .setAmount(amount * 100)
                    .build();

            Refund refund = Refund.create(params);

            logger.info("Stripe refund created: {}",
                refund.getId());

            response.put("success", true);
            response.put("refundId", refund.getId());
            response.put("status", refund.getStatus());
            response.put("amount", amount);

        } catch (StripeException e) {
            logger.error("Stripe refund error: {}",
                e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }
}
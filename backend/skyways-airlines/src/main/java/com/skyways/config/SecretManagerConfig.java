package com.skyways.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class SecretManagerConfig {

    private static final Logger logger =
        LoggerFactory.getLogger(SecretManagerConfig.class);

    @Value("${skyscanner.api.key}")
    private String skyscannerKey;

    @Value("${sendgrid.api.key}")
    private String sendgridKey;

    @Value("${stripe.api.key}")
    private String stripeKey;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @PostConstruct
    public void validateSecrets() {
        logger.info("=== Secret Manager Validation ===");
        logger.info("Skyscanner API Key: {}",
            maskSecret(skyscannerKey));
        logger.info("SendGrid API Key: {}",
            maskSecret(sendgridKey));
        logger.info("Stripe API Key: {}",
            maskSecret(stripeKey));
        logger.info("JWT Secret: {}",
            maskSecret(jwtSecret));
        logger.info("All secrets loaded successfully!");
    }

    private String maskSecret(String secret) {
        if (secret == null || secret.length() < 8) {
            return "***";
        }
        return secret.substring(0, 4) + 
            "****" + 
            secret.substring(secret.length() - 4);
    }
}
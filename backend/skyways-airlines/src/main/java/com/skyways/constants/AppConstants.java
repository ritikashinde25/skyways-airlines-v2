package com.skyways.constants;

public final class AppConstants {

    private AppConstants() {}

    // JWT
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    // User Status
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    // Booking Status
    public static final String BOOKING_CONFIRMED = "CONFIRMED";
    public static final String BOOKING_CANCELLED = "CANCELLED";
    public static final String BOOKING_PENDING = "PENDING";

    // Payment Status
    public static final String PAYMENT_SUCCESS = "SUCCESS";
    public static final String PAYMENT_FAILED = "FAILED";
    public static final String PAYMENT_REFUNDED = "REFUNDED";

    // Notification Status
    public static final String NOTIFICATION_SENT = "SENT";
    public static final String NOTIFICATION_FAILED = "FAILED";

    // Kafka Topics
    public static final String TOPIC_BOOKING_CREATED = "booking-created";
    public static final String TOPIC_PAYMENT_PROCESSED = "payment-processed";
    public static final String TOPIC_NOTIFICATION_SEND = "notification-send";

    // Flight Classes
    public static final String CLASS_ECONOMY = "Economy";
    public static final String CLASS_PREMIUM_ECONOMY = "Premium Economy";
    public static final String CLASS_BUSINESS = "Business";

    // Seat Types
    public static final String SEAT_WINDOW = "Window";
    public static final String SEAT_AISLE = "Aisle";
    public static final String SEAT_MIDDLE = "Middle";

    // Seat Charges
    public static final double SEAT_WINDOW_CHARGE = 1000.0;
    public static final double SEAT_AISLE_CHARGE = 500.0;
    public static final double SEAT_MIDDLE_CHARGE = 0.0;

    // Class Multipliers
    public static final double ECONOMY_MULTIPLIER = 1.0;
    public static final double PREMIUM_ECONOMY_MULTIPLIER = 1.5;
    public static final double BUSINESS_MULTIPLIER = 2.5;

    // API Messages
    public static final String SUCCESS_REGISTER = "User registered successfully";
    public static final String SUCCESS_LOGIN = "Login successful";
    public static final String SUCCESS_BOOKING = "Booking confirmed successfully";
    public static final String SUCCESS_PAYMENT = "Payment processed successfully";
    public static final String SUCCESS_CANCEL = "Booking cancelled successfully";
    public static final String SUCCESS_REFUND = "Payment refunded successfully";

    // Error Messages
    public static final String ERROR_USER_NOT_FOUND = "User not found";
    public static final String ERROR_FLIGHT_NOT_FOUND = "Flight not found";
    public static final String ERROR_BOOKING_NOT_FOUND = "Booking not found";
    public static final String ERROR_PAYMENT_NOT_FOUND = "Payment not found";
    public static final String ERROR_DUPLICATE_USERNAME = "Username already exists";
    public static final String ERROR_DUPLICATE_EMAIL = "Email already exists";
    public static final String ERROR_INVALID_PASSWORD = "Invalid password";
    public static final String ERROR_FIELDS_REQUIRED = "All fields are required";
}
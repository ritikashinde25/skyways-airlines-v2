package com.skyways.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Flight ID is required")
    private String flightId;

    @NotBlank(message = "Flight number is required")
    private String flightNumber;

    @NotBlank(message = "Origin is required")
    private String origin;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotBlank(message = "Booking date is required")
    private String bookingDate;

    @NotNull(message = "Total price is required")
    private Double totalPrice;

    private String travelClass;
    private String seatType;
    private String email;
}
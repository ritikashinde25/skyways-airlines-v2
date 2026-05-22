package com.skyways.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchDTO {

    private String origin;
    private String destination;
    private String departureDate;
    private Integer passengers;
    private String travelClass;
}
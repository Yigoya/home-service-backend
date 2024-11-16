package com.home.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequestDTO {
    private Long customerId;
    private Long technicianId;
    private Long serviceId;
    private String description;

    // Getters and Setters
}

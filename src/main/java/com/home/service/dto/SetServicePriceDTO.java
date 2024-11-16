package com.home.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetServicePriceDTO {
    private Long technicianId;
    private Long serviceId;
    private Double price;

    // Getters and Setters
}

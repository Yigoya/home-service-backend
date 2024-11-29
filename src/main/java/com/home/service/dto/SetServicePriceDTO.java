package com.home.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetServicePriceDTO {
    @NotNull(message = "Technician ID cannot be null")
    private Long technicianId;

    @NotNull(message = "Service ID cannot be null")
    private Long serviceId;

    @NotNull(message = "Price cannot be null")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private Double price;

    // Getters and Setters
}

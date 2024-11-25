package com.home.service.dto;

import java.time.LocalTime;

import com.home.service.models.enums.EthiopianLanguage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Estimated duration is required")
    private LocalTime estimatedDuration;

    @NotNull(message = "Service fee is required")
    @Positive(message = "Service fee must be positive")
    private Double serviceFee;

    @NotNull(message = "Language is required")
    private EthiopianLanguage lang;

}

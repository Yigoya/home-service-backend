package com.home.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {
    @NotNull(message = "Please provide a valid Customer ID.")
    private Long customerId;

    @NotNull(message = "Please provide a valid Technician ID.")
    private Long technicianId;

    @NotNull(message = "Please provide a valid Service ID.")
    private Long serviceId;

    @NotBlank(message = "Description is required and cannot be blank.")
    @Size(max = 500, message = "Description cannot exceed 500 characters.")
    private String description;

    @NotNull(message = "Please provide a valid Scheduled Date.")
    private LocalDateTime scheduledDate;

    private String street;

    private String city;

    @NotBlank(message = "Subcity is required and cannot be blank.")
    private String subcity;

    @NotBlank(message = "Wereda is required and cannot be blank.")
    private String wereda;

    private String state;

    private String country;

    private String zipCode;

    private Double latitude;

    private Double longitude;

    private String timeSchedule;
}

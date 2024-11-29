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

    @NotBlank(message = "Street is required and cannot be blank.")
    private String street;

    @NotBlank(message = "City is required and cannot be blank.")
    private String city;

    @NotBlank(message = "Subcity is required and cannot be blank.")
    private String subcity;

    @NotBlank(message = "Wereda is required and cannot be blank.")
    private String wereda;

    @NotBlank(message = "State is required and cannot be blank.")
    private String state;

    @NotBlank(message = "Country is required and cannot be blank.")
    private String country;

    @NotBlank(message = "Zip code is required and cannot be blank.")
    private String zipCode;

    @NotNull(message = "Please provide a valid Latitude.")
    private Double latitude;

    @NotNull(message = "Please provide a valid Longitude.")
    private Double longitude;

    @NotBlank(message = "Time schedule is required and cannot be blank.")
    private String timeSchedule;
}

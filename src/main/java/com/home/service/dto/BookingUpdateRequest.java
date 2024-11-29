package com.home.service.dto;

import java.time.LocalDateTime;

import com.home.service.models.enums.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingUpdateRequest {
    @NotNull(message = "Scheduled date cannot be null")
    private LocalDateTime scheduledDate;

    @NotBlank(message = "Street cannot be blank")
    @Size(max = 255, message = "Street cannot exceed 255 characters")
    private String street;

    @NotBlank(message = "City cannot be blank")
    @Size(max = 255, message = "City cannot exceed 255 characters")
    private String city;

    @NotBlank(message = "Subcity cannot be blank")
    @Size(max = 255, message = "Subcity cannot exceed 255 characters")
    private String subcity;

    @NotBlank(message = "Wereda cannot be blank")
    @Size(max = 255, message = "Wereda cannot exceed 255 characters")
    private String wereda;

    @NotBlank(message = "State cannot be blank")
    @Size(max = 255, message = "State cannot exceed 255 characters")
    private String state;

    @NotBlank(message = "Country cannot be blank")
    @Size(max = 255, message = "Country cannot exceed 255 characters")
    private String country;

    @NotBlank(message = "Zip code cannot be blank")
    @Size(max = 20, message = "Zip code cannot exceed 20 characters")
    private String zipCode;

    @NotNull(message = "Latitude cannot be null")
    private Double latitude;

    @NotNull(message = "Longitude cannot be null")
    private Double longitude;

    @NotBlank(message = "Time schedule cannot be blank")
    @Size(max = 255, message = "Time schedule cannot exceed 255 characters")
    private String timeSchedule;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotNull(message = "Status cannot be null")
    private BookingStatus status;
}

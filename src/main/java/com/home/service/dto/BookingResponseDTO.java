package com.home.service.dto;

import java.time.LocalDateTime;

import com.home.service.models.enums.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponseDTO {
    private Long id;
    private String customerName;
    private String technicianName;
    private String technicianProfleImage;
    private String customerProfleImage;
    private String serviceName;
    private LocalDateTime scheduledDate;
    private BookingStatus status;
    private String description;
    private AddressDTO address;
    private ReviewDTO review;
    // Constructors, Getters, and Setters
}

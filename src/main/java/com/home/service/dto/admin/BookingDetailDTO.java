package com.home.service.dto.admin;

import java.time.LocalDateTime;

import com.home.service.models.enums.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDetailDTO {
    private Long bookingId;
    private String description;
    private LocalDateTime createdAt;
    private String timeSchedule;
    private String service;
    private BookingStatus status;
    private AddressDTO serviceLocation;
    private CustomerDTO customer;
    private TechnicianDTO technician;
    private ReviewDTO review;

    // Getters and Setters
}

package com.home.service.dto;

import com.home.service.models.enums.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookingStatusDTO {
    private Long bookingId;
    private BookingStatus status;

    // Getters and Setters
}

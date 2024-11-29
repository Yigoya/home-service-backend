package com.home.service.dto;

import com.home.service.models.enums.BookingStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookingStatusDTO {
    @NotNull(message = "Booking ID cannot be null")
    private Long bookingId;

    @NotNull(message = "Booking status cannot be null")
    private BookingStatus status;
}

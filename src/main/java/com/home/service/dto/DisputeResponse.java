package com.home.service.dto;

import com.home.service.models.Booking;
import com.home.service.models.Customer;
import com.home.service.models.Technician;
import com.home.service.models.enums.DisputeStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisputeResponse {
    private Customer customer;
    private Technician technician;
    private String disputeReason;
    private DisputeStatus disputeStatus;
    private Booking booking;
}

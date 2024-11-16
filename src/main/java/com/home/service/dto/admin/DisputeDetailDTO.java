package com.home.service.dto.admin;

import java.time.LocalDateTime;

import com.home.service.models.enums.DisputeStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisputeDetailDTO {
    private Long disputeId;
    private String reason;
    private String description;
    private LocalDateTime createdAt;
    private DisputeStatus status;
    private BookingDetailDTO booking;
    private CustomerDTO customer;
    private TechnicianDTO technician;
}

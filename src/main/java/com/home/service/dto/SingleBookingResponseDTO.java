package com.home.service.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleBookingResponseDTO {

    private Long bookingId;
    private Long customerId;
    private String customerName;
    private Long technicianId;
    private String technicianName;
    private Long serviceId;
    private String serviceName;
    private String serviceDescription;
    private LocalDateTime scheduledDate;
    private String status;
    private AddressDTO serviceLocation;
    private Double totalCost;
    private String timeSchedule;
    private List<QuestionWithAnswerDTO> questions;

    // Getters and setters
}

package com.home.service.dto.admin;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceWithCountsDTO {
    private Long serviceId;
    private String name;
    private String description;
    private LocalTime estimatedDuration;
    private Double serviceFee;
    private Integer technicianCount;
    private Integer bookingCount;

}

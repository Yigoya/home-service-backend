package com.home.service.dto.admin;

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
    private String estimatedDuration;
    private Double serviceFee;
    private Integer technicianCount;
    private Integer bookingCount;

}

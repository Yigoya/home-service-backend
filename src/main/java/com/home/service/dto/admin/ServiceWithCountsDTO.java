package com.home.service.dto.admin;

import java.time.LocalTime;
import java.util.List;

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
    private String icon;
    private String document;
    private Long categoryId;
    private List<ServiceWithCountsDTO> services;

}

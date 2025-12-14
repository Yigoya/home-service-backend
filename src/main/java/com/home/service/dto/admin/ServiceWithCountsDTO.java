package com.home.service.dto.admin;

import java.time.LocalTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceWithCountsDTO {
    private Long serviceId;
    private String name;
    @JsonIgnore
    private String description;
    @JsonIgnore
    private LocalTime estimatedDuration;
    @JsonIgnore
    private Double serviceFee;
    @JsonIgnore
    private Integer technicianCount;
    @JsonIgnore
    private Integer bookingCount;
    private String icon;
    @JsonIgnore
    private String document;
    private Long categoryId;
    private List<ServiceWithCountsDTO> services;

}

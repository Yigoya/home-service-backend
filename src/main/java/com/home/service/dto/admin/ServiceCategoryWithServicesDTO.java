package com.home.service.dto.admin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCategoryWithServicesDTO {
    private Long categoryId;
    private String categoryName;
    @JsonIgnore
    private String description;

    @JsonIgnore
    private String icon;
    private List<ServiceWithCountsDTO> services;

    private Long order;

    // Getters and Setters
}

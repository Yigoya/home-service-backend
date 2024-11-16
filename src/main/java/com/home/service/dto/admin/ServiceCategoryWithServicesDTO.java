package com.home.service.dto.admin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCategoryWithServicesDTO {
    private Long categoryId;
    private String categoryName;
    private String description;
    private List<ServiceWithCountsDTO> services;

    // Getters and Setters
}

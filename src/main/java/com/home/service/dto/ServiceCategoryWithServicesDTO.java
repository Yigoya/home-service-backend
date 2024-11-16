package com.home.service.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.home.service.models.ServiceCategory;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCategoryWithServicesDTO {
    private Long id;
    private String name;
    private String description;
    private List<ServiceDTO> services;

    public ServiceCategoryWithServicesDTO(ServiceCategory serviceCategory, List<ServiceDTO> services) {

        // Initialize fields using the serviceCategory object
        this.id = serviceCategory.getId();
        this.name = serviceCategory.getCategoryName();
        this.description = serviceCategory.getDescription();
        this.services = services;

    }
}

package com.home.service.dto;

import com.home.service.models.Services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer duration;

    public ServiceDTO(Services service) {

        // Initialize fields using the service object
        this.id = service.getId();
        this.name = service.getName();
        this.description = service.getDescription();

    }
}

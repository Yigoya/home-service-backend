package com.home.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TechnicianSearchFilterDTO {
    private String name;
    private String city;
    private String subcity;
    private Double minLatitude;
    private Double maxLatitude;
    private Double minLongitude;
    private Double maxLongitude;
    private Double minPrice;
    private Double maxPrice;

    // Getters and Setters
}

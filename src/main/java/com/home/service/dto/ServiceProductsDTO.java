package com.home.service.dto;

import java.time.LocalTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProductsDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private LocalTime duration;
    private Long categoryId;
    private String icon;
    private List<ProductDTO> products;
}

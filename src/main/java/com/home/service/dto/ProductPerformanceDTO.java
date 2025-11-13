package com.home.service.dto;

import lombok.Data;

@Data
public class ProductPerformanceDTO {
    private Long productId;
    private String productName;
    private Long views;
    private Long inquiries;
    private Long orders;
    private Double totalRevenue;
}

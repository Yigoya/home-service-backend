package com.home.service.dto;

import java.util.List;
import java.util.Set;

import com.home.service.models.enums.ProductCondition;

import lombok.Data;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String currency;
    private Integer stockQuantity;
    private Integer minOrderQuantity;
    private List<String> images;
    private String category;
    private String sku;
    private boolean isActive;
    private Long businessId;
    private String businessName;
    private String businessEmail;
    private String businessPhoneNumber;
    private String businessWebsite;
    private String businessLogo;
    private String specifications;
    private Set<Long> serviceIds;
    private ProductCondition condition;
}

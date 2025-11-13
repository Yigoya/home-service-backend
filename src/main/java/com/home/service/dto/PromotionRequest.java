package com.home.service.dto;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.models.enums.PromotionType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromotionRequest {
    private Long businessId;
    private String title;
    private String description;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private PromotionType type;
    private Double discountPercentage;
    private Boolean isFeatured = false;
    private Set<Long> serviceIds;
    private MultipartFile image; // Optional image file
    private String termsAndConditions;
}

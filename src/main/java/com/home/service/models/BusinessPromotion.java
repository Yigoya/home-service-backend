package com.home.service.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import com.home.service.models.enums.PromotionType;

@Entity
@Table(name = "business_promotions")
@Getter
@Setter
public class BusinessPromotion extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    private String title;

    private String description;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private PromotionType type;

    private Double discountPercentage;

    // Getters and Setters
}

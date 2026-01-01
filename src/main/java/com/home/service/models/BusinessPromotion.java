package com.home.service.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.home.service.models.enums.PromotionType;
import com.home.service.models.ServiceCategory;

@Entity
@Table(name = "business_promotions")
@Getter
@Setter
public class BusinessPromotion extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    private String title;

        @ManyToOne
        @JoinColumn(name = "category_id")
        private ServiceCategory category;

    private String description;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private PromotionType type;

    private Double discountPercentage;

    private boolean isFeatured = false;

    @ManyToMany
    @JoinTable(name = "promotion_services",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"))
    private Set<Services> services = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "promotion_images", joinColumns = @JoinColumn(name = "promotion_id"))
    @Column(name = "image_url")
    private List<String> images;

    private String termsAndConditions;

    // Getters and Setters
}

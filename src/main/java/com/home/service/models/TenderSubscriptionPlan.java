package com.home.service.models;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// TenderSubscriptionPlan.java
@Entity
@Getter
@Setter
@AllArgsConstructor
public class TenderSubscriptionPlan extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String planId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String duration;

    @ElementCollection
    private List<String> features;

    private Boolean isActive = true;

    public TenderSubscriptionPlan(String planId, String name, double price, String duration, List<String> features) {
        this.planId = planId;
        this.name = name;
        this.price = price;
        this.duration = duration;
        this.features = features;
        this.isActive = true;
    }
}

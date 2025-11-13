package com.home.service.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

import com.home.service.models.enums.PlanType;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // e.g., "Basic", "Pro", "Elite"

    @Column(nullable = false)
    private BigDecimal price; // Price in ETB

    @Column(nullable = false)
    private Integer durationMonths; // 1, 3, 6, 12

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType planType; // TECHNICIAN, BUSINESS, CUSTOMER_TENDER

    @ElementCollection
    @CollectionTable(name = "subscription_features_english", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "feature")
    private List<String> featuresEnglish; // Features in English

    @ElementCollection
    @CollectionTable(name = "subscription_features_amharic", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "feature")
    private List<String> featuresAmharic; // Features in Amharic (አማርኛ)

    @ElementCollection
    @CollectionTable(name = "subscription_features_oromo", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "feature")
    private List<String> featuresOromo; // Features in Oromo (Afaan Oromoo)

    // Constructors
    public SubscriptionPlan() {
    }

    public SubscriptionPlan(String name, BigDecimal price, Integer durationMonths, PlanType planType,
            List<String> featuresEnglish, List<String> featuresAmharic, List<String> featuresOromo) {
        this.name = name;
        this.price = price;
        this.durationMonths = durationMonths;
        this.planType = planType;
        this.featuresEnglish = featuresEnglish;
        this.featuresAmharic = featuresAmharic;
        this.featuresOromo = featuresOromo;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
    }

    public PlanType getPlanType() {
        return planType;
    }

    public void setPlanType(PlanType planType) {
        this.planType = planType;
    }

    public List<String> getFeaturesEnglish() {
        return featuresEnglish;
    }

    public void setFeaturesEnglish(List<String> featuresEnglish) {
        this.featuresEnglish = featuresEnglish;
    }

    public List<String> getFeaturesAmharic() {
        return featuresAmharic;
    }

    public void setFeaturesAmharic(List<String> featuresAmharic) {
        this.featuresAmharic = featuresAmharic;
    }

    public List<String> getFeaturesOromo() {
        return featuresOromo;
    }

    public void setFeaturesOromo(List<String> featuresOromo) {
        this.featuresOromo = featuresOromo;
    }
}

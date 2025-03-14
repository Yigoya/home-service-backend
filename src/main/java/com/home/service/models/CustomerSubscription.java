package com.home.service.models;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.home.service.models.enums.SubscriptionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CustomerSubscription extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private TenderSubscriptionPlan plan;

    @Column(nullable = false)
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    private String whatsappNumber;
    private String telegramUsername;

    @ElementCollection
    private Set<Long> followedServiceIds = new HashSet<>();

    private String companyName;
    private String tinNumber;

    private LocalDateTime lastNotificationSent;

    // Getters, Setters, Constructors
}
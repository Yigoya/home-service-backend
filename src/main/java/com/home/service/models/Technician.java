package com.home.service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Technician extends BaseEntity {
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Lob
    @Column(length = 512)
    private String bio;
    private String availability = "Available";
    private Double rating;
    private Integer completedJobs;

    // Optional business name for the technician
    private String businessName;

    // Years of professional experience
    private Integer yearsExperience;

    // Public-facing service area description (e.g., city or districts served)
    private String serviceArea;

    @ElementCollection
    private List<String> documents;

    // Stored file paths for professional licenses
    @ElementCollection
    private List<String> licenses;

    private String idCardImage;

    @OneToOne(mappedBy = "technician", cascade = CascadeType.ALL, orphanRemoval = true)
    private TechnicianWeeklySchedule weeklySchedule;

    @OneToMany(mappedBy = "technician", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TechnicianServicePrice> servicePrices;

    @ManyToMany
    @JoinTable(name = "technician_services", joinColumns = @JoinColumn(name = "technician_id"), inverseJoinColumns = @JoinColumn(name = "service_id"))
    private Set<Services> services;

    private Boolean verified = false;

    @OneToMany(mappedBy = "technician", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TechnicianAddress> technicianAddresses = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "subscription_plan_id")
    private SubscriptionPlan subscriptionPlan;

    @OneToMany(mappedBy = "technician", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TechnicianPortfolio> portfolio = new ArrayList<>();

    // Social media and website (optional)
    private String website;
    private String facebook;
    private String twitter;
    private String instagram;
    private String linkedin;
    private String whatsapp;
}

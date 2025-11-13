package com.home.service.models;

import java.util.HashSet;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;

import java.util.List;
import java.util.Set;

import com.home.service.models.enums.BusinessType;
import com.home.service.models.enums.OpeningHours;
import com.home.service.models.enums.SocialMedia;


@Entity
@Table(name = "businesses")
@Getter
@Setter
public class Business extends BaseEntity {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    @Column(length = 5000)
    private String description;

    private String logo;

    private String website;

    private Integer foundedYear;

    private Integer employeeCount;

    // Primary verified flag mapped to new column
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    // Temporary bridge for legacy schema: keep old `verified` column in sync to satisfy NOT NULL constraints.
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "verified", nullable = false)
    private boolean legacyVerified;

    private String industry;

    private String taxId;

    private String certifications;
    private Integer minOrderQuantity;
    private String tradeTerms;

    @ManyToMany
    @JoinTable(name = "company_services",
            joinColumns = @JoinColumn(name = "company_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"))
    private Set<Services> services = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "location_id")
    private BusinessLocation location;

    @Embedded
    private SocialMedia socialMedia;

    @Embedded
    private OpeningHours openingHours;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @ElementCollection
    private List<String> images;

    private boolean isFeatured;

    @ManyToOne
    @JoinColumn(name = "subscription_plan_id")
    private SubscriptionPlan subscriptionPlan;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<B2BOrder> orders;

    @PrePersist
    private void syncVerifiedOnPersist() {
        // Ensure both columns are written consistently
        this.legacyVerified = this.isVerified;
    }

    @PreUpdate
    private void syncVerifiedOnUpdate() {
        this.legacyVerified = this.isVerified;
    }
}

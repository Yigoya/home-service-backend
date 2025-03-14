package com.home.service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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

    private boolean verified;

    private String industry;

    private String taxId;

    @ManyToMany
    @JoinTable(name = "company_categories", joinColumns = @JoinColumn(name = "company_id"), inverseJoinColumns = @JoinColumn(name = "service_id"))
    private List<Services> categories;

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

    private boolean isVerified;

    private boolean isFeatured;

    // Getters and Setters
}

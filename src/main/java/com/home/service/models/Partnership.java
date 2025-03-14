package com.home.service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import com.home.service.models.enums.PartnershipStatus;
import com.home.service.models.enums.PartnershipType;

@Entity
@Table(name = "partnerships")
@Getter
@Setter
public class Partnership extends BaseEntity {

    @NotBlank
    private String partnerName;

    private String partnerLogo;

    private String partnerWebsite;

    @Enumerated(EnumType.STRING)
    private PartnershipType partnershipType;

    private String startDate;

    private String endDate;

    @Enumerated(EnumType.STRING)
    private PartnershipStatus status;

    @Column(length = 5000)
    private String description;

    private String contactPerson;

    @Email
    private String contactEmail;

    private String contactPhone;

    @ElementCollection
    private List<String> agreements;

    @ElementCollection
    private List<String> benefits;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Business company;

    // Getters and Setters
}

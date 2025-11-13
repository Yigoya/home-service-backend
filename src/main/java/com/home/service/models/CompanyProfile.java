package com.home.service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "company_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProfile {

    @Id
    private Long id; // Same ID as the User

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_location")
    private String location;
    private String logo;
    private String coverImage;
    private String description;
    private String industry;
    private String size;
    private String founded;
    private String website;
    private String email;
    private String phone;
    private Double rating;
    private Integer totalReviews;
    private Integer openJobs;
    private Integer totalHires;
    
    // Additional company registration fields
    private String companyType;
    private String country;
    private String city;
    private String businessLicense;
    private String linkedinPage;
    
    // Company documents
    private String companyDocuments;
    
    // Contact person information
    private String contactPersonFullName;
    private String contactPersonJobTitle;
    private String contactPersonWorkEmail;
    private String contactPersonWorkPhone;
    
    @ElementCollection
    @CollectionTable(name = "company_benefits", joinColumns = @JoinColumn(name = "company_id"))
    @Column(name = "benefit")
    private java.util.List<String> benefits;
}
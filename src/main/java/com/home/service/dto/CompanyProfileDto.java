package com.home.service.dto;

import lombok.Data;
import java.util.List;

@Data
public class CompanyProfileDto {
    private Long companyId;
    private String name;
    private String location;
    private String description;
    private String logo;
    private String coverImage;
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
    private List<String> benefits;
    
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
}
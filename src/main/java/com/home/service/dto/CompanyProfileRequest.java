package com.home.service.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class CompanyProfileRequest {
    private Long userId;
    private String name;
    private String location;
    private String description;
    private MultipartFile logo;
    private MultipartFile coverImage;
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
    private MultipartFile companyDocuments;
    
    // Contact person information
    private String contactPersonFullName;
    private String contactPersonJobTitle;
    private String contactPersonWorkEmail;
    private String contactPersonWorkPhone;
}
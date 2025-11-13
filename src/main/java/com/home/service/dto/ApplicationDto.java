package com.home.service.dto;

import java.time.Instant;

import lombok.Data;

@Data
public class ApplicationDto {
    private Long id;
    private String candidateName;
    private String email;
    private String phone;
    private String jobTitle;
    private String appliedDate;
    private String status;
    private String experience;
    private String location;
    private String avatar;
    private String resumeUrl;
    private String coverLetter;
    private Integer rating;
    
    // Additional fields for getMyApplications
    private String companyName;
    private String companyLogo;
    private String jobType;
    private String salaryRange;
    
    // Keep original fields for backward compatibility
    private Long jobId;
    private Long jobSeekerId;
    private String jobSeekerName;
    private Instant applicationDate;
}
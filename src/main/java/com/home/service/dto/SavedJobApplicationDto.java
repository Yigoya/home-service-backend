package com.home.service.dto;

import lombok.Data;

@Data
public class SavedJobApplicationDto {
    private Long id;
    private String candidateName;
    private String email;
    private String phone;
    private String jobTitle;
    private String appliedDate;
    private String status; // 'pending' | 'reviewed' | 'shortlisted' | 'rejected' | 'hired'
    private String experience;
    private String location;
    private String avatar;
    private String resumeUrl;
    private String coverLetter;
    private String savedDate;
    private String jobSeekerId;
    private String jobApplicationId;
}
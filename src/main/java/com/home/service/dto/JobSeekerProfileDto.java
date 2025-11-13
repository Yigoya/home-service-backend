package com.home.service.dto;

import java.util.List;
import java.util.Set;

import com.home.service.models.enums.AccountStatus;
import com.home.service.models.enums.EthiopianLanguage;
import com.home.service.models.enums.UserRole;
import lombok.Data;

@Data
public class JobSeekerProfileDto {
    // Job Seeker Profile fields
    private Long userId;
    private String headline;
    private String summary;
    private Set<String> skills;
    private String resumeUrl;
    
    // User fields
    private String name;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String phone; // alias for phoneNumber
    private String email;
    private UserRole role;
    private String profileImage;
    private AccountStatus status;
    private EthiopianLanguage preferredLanguage;
    
    // Additional profile fields
    private String location;
    private String title; // alias for headline
    private String bio; // alias for summary
    private List<ExperienceDto> experience;
    private List<EducationDto> education;
    private String linkedin;
    private String github;
}
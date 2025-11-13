package com.home.service.dto;

import java.util.Set;

import com.home.service.models.enums.EthiopianLanguage;
import lombok.Data;

@Data
public class UpdateJobSeekerProfileRequest {
    // Job Seeker Profile fields
    private String headline;
    private String summary;
    private Set<String> skills;
    
    // User fields that can be updated
    private String name;
    private String phoneNumber;
    private EthiopianLanguage preferredLanguage;
}
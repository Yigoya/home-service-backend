package com.home.service.dto;

import org.springframework.stereotype.Component;
import com.home.service.models.SavedJobApplication;
import java.time.format.DateTimeFormatter;

@Component
public class SavedJobApplicationMapper {

    public SavedJobApplicationDto toSavedJobApplicationDto(SavedJobApplication savedJobApplication) {
        if (savedJobApplication == null) {
            return null;
        }

        SavedJobApplicationDto dto = new SavedJobApplicationDto();
        
        // Main ID
        dto.setId(savedJobApplication.getId());
        
        // Candidate information
        dto.setCandidateName(savedJobApplication.getJobApplication().getJobSeeker().getName());
        dto.setEmail(savedJobApplication.getJobApplication().getJobSeeker().getEmail());
        dto.setPhone(savedJobApplication.getJobApplication().getJobSeeker().getPhoneNumber() != null ? 
                    savedJobApplication.getJobApplication().getJobSeeker().getPhoneNumber() : "");
        
        // Job details
        dto.setJobTitle(savedJobApplication.getJobApplication().getJob().getTitle());
        
        // Application details - convert dates to strings
        dto.setAppliedDate(savedJobApplication.getJobApplication().getApplicationDate() != null ? 
                          savedJobApplication.getJobApplication().getApplicationDate().toString().substring(0, 10) : "");
        
        dto.setStatus(savedJobApplication.getJobApplication().getStatus() != null ? 
                     savedJobApplication.getJobApplication().getStatus().toString().toLowerCase() : "pending");
        
        dto.setCoverLetter(savedJobApplication.getJobApplication().getCoverLetter() != null ? 
                          savedJobApplication.getJobApplication().getCoverLetter() : "");
        dto.setResumeUrl(savedJobApplication.getJobApplication().getResumeUrl() != null ? 
                        savedJobApplication.getJobApplication().getResumeUrl() : "");
        
        dto.setSavedDate(savedJobApplication.getSavedAt() != null ? 
                        savedJobApplication.getSavedAt().toString().substring(0, 10) : "");
        
        // Additional candidate info (with defaults if not available)
        dto.setExperience("Experience not specified");
        dto.setLocation("Location not specified");
        dto.setAvatar(savedJobApplication.getJobApplication().getJobSeeker().getProfileImage() != null ? 
                     savedJobApplication.getJobApplication().getJobSeeker().getProfileImage() : "");
        
        // IDs as strings
        dto.setJobSeekerId(savedJobApplication.getJobApplication().getJobSeeker().getId().toString());
        dto.setJobApplicationId(savedJobApplication.getJobApplication().getId().toString());
        
        return dto;
    }
}
package com.home.service.dto;

import org.springframework.stereotype.Component;
import com.home.service.models.JobApplication;

@Component
public class JobApplicationMapper {

    public JobApplicationDto toJobApplicationDto(JobApplication jobApplication) {
        if (jobApplication == null) {
            return null;
        }

        JobApplicationDto dto = new JobApplicationDto();
        dto.setId(jobApplication.getId());
        dto.setJobId(jobApplication.getJob().getId());
        dto.setJobTitle(jobApplication.getJob().getTitle());
        dto.setCompanyName(jobApplication.getJob().getCompany().getName());
        dto.setJobSeekerId(jobApplication.getJobSeeker().getId());
        dto.setJobSeekerName(jobApplication.getJobSeeker().getName());
        dto.setJobSeekerEmail(jobApplication.getJobSeeker().getEmail());
        dto.setCoverLetter(jobApplication.getCoverLetter());
        dto.setResumeUrl(jobApplication.getResumeUrl());
        dto.setStatus(jobApplication.getStatus());
        dto.setRating(jobApplication.getRating());
        dto.setApplicationDate(jobApplication.getApplicationDate());
        
        return dto;
    }
}
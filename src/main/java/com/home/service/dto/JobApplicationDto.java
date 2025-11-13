package com.home.service.dto;

import lombok.Data;
import com.home.service.models.enums.ApplicationStatus;
import java.time.Instant;

@Data
public class JobApplicationDto {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private Long jobSeekerId;
    private String jobSeekerName;
    private String jobSeekerEmail;
    private String coverLetter;
    private String resumeUrl;
    private ApplicationStatus status;
    private Integer rating;
    private Instant applicationDate;
}
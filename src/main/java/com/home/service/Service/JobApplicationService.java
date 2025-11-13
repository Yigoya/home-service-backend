package com.home.service.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.home.service.config.exceptions.ResourceNotFoundException;
import com.home.service.dto.ApplicationRequestDto;
import com.home.service.models.CompanyProfile;
import com.home.service.models.Job;
import com.home.service.models.JobApplication;
import com.home.service.models.User;
import com.home.service.models.enums.ApplicationStatus;
import com.home.service.repositories.CompanyProfileRepository;
import com.home.service.repositories.JobApplicationRepository;
import com.home.service.repositories.JobRepository;
import com.home.service.repositories.UserRepository;
import com.home.service.repositories.SavedJobApplicationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final SavedJobApplicationRepository savedJobApplicationRepository;

    @Transactional
    public JobApplication applyToJob(Long jobId, ApplicationRequestDto requestDto, Long userId) {
        User jobSeeker = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        // Optional: Check if already applied
        if (applicationRepository.existsByJobIdAndJobSeekerId(jobId, jobSeeker.getId())) {
            throw new IllegalStateException("You have already applied for this job.");
        }

        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setJobSeeker(jobSeeker);
        application.setCoverLetter(requestDto.getCoverLetter());
        application.setResumeUrl(requestDto.getResumeUrl());
        application.setStatus(ApplicationStatus.PENDING);

        return applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public List<JobApplication> getApplicationsForJob(Long jobId, Long userId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        // Verify the user requesting is the one who posted the job
        if (!job.getCompany().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to view applications for this job.");
        }

        return applicationRepository.findByJobId(jobId);
    }

    @Transactional(readOnly = true)
    public List<JobApplication> getApplicationsBySeeker(Long seekerId) {
        User jobSeeker = userRepository.findById(seekerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", seekerId));

        return applicationRepository.findByJobSeekerId(jobSeeker.getId());
    }

    @Transactional(readOnly = true)
    public Page<JobApplication> getApplicationsBySeeker(Long seekerId, Pageable pageable) {
        User jobSeeker = userRepository.findById(seekerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", seekerId));

        return applicationRepository.findByJobSeekerId(jobSeeker.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<JobApplication> getApplicationsByCompany(Long companyId, 
                                                        ApplicationStatus status, 
                                                        String jobTitle, 
                                                        Integer rating, 
                                                        Pageable pageable) {
        // Verify the company exists
        companyProfileRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId));

        // Apply filters based on provided parameters
        if (status != null && jobTitle != null && !jobTitle.trim().isEmpty()) {
            return applicationRepository.findByCompanyIdAndStatusAndJobTitle(companyId, status, jobTitle.trim(), pageable);
        } else if (status != null) {
            return applicationRepository.findByCompanyIdAndStatus(companyId, status, pageable);
        } else if (jobTitle != null && !jobTitle.trim().isEmpty()) {
            return applicationRepository.findByCompanyIdAndJobTitle(companyId, jobTitle.trim(), pageable);
        } else if (rating != null) {
            return applicationRepository.findByCompanyIdAndRating(companyId, rating, pageable);
        } else {
            return applicationRepository.findByCompanyId(companyId, pageable);
        }
    }

    @Transactional
    public JobApplication getApplicationForCompanyWithAutoStatus(Long companyId, Long applicationId) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", "id", applicationId));

        Long applicationCompanyId = application.getJob().getCompany().getId();
        if (!applicationCompanyId.equals(companyId)) {
            throw new AccessDeniedException("You do not have permission to view this application");
        }

        // Do not override terminal states
        if (application.getStatus() != ApplicationStatus.HIRED && application.getStatus() != ApplicationStatus.REJECTED) {
            // If the company saved this application -> SHORTLISTED
            User companyUser = application.getJob().getCompany().getUser();
            boolean saved = savedJobApplicationRepository.existsByCompanyUserAndJobApplicationId(companyUser, applicationId);
            if (saved && application.getStatus() != ApplicationStatus.SHORTLISTED) {
                application.setStatus(ApplicationStatus.SHORTLISTED);
                application = applicationRepository.save(application);
            } else if (!saved && application.getStatus() == ApplicationStatus.PENDING) {
                // Mark as reviewed when company views it
                application.setStatus(ApplicationStatus.REVIEWED);
                application = applicationRepository.save(application);
            }
        }

        return application;
    }

    @Transactional
    public JobApplication updateApplicationStatus(Long companyId, Long applicationId, ApplicationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status must be provided");
        }

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", "id", applicationId));

        Long applicationCompanyId = application.getJob().getCompany().getId();
        if (!applicationCompanyId.equals(companyId)) {
            throw new AccessDeniedException("You do not have permission to update this application");
        }

        application.setStatus(status);
        return applicationRepository.save(application);
    }
}
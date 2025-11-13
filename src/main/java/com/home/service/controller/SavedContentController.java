package com.home.service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.home.service.Service.SavedContentService;
import com.home.service.dto.JobApplicationDto;
import com.home.service.dto.JobApplicationMapper;
import com.home.service.dto.SavedBusinessDto;
import com.home.service.dto.SavedBusinessSummaryDto;
import com.home.service.dto.JobMapper;
import com.home.service.dto.JobSeekerProfileDto;
import com.home.service.dto.JobSummaryDto;
import com.home.service.dto.ProfileMapper;
import com.home.service.dto.SavedJobApplicationDto;
import com.home.service.dto.SavedJobApplicationMapper;
import com.home.service.models.Job;
import com.home.service.models.JobApplication;
import com.home.service.models.JobSeekerProfile;
import com.home.service.models.SavedBusiness;
import com.home.service.models.SavedJob;
import com.home.service.models.SavedJobApplication;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/saved")
@RequiredArgsConstructor
public class SavedContentController {

    private final SavedContentService savedContentService;
    private final JobMapper jobMapper;
    private final ProfileMapper profileMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final SavedJobApplicationMapper savedJobApplicationMapper;

    // --- Endpoints for Job Seekers to manage saved jobs ---
    @CrossOrigin(originPatterns = "*")
    @PostMapping("{jobSeekerId}/jobs/{jobId}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<String> saveJob(@PathVariable Long jobSeekerId, @PathVariable Long jobId) {
        savedContentService.saveJob(jobId, jobSeekerId);
        return ResponseEntity.ok("Job saved successfully");
    }
    @CrossOrigin(originPatterns = "*")
@DeleteMapping("{jobSeekerId}/jobs/{jobId}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Void> unsaveJob(@PathVariable Long jobSeekerId, @PathVariable Long jobId) {
        savedContentService.unsaveJob(jobId, jobSeekerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{jobSeekerId}/jobs")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Page<JobSummaryDto>> getSavedJobs(@PathVariable Long jobSeekerId, Pageable pageable) {
        Page<SavedJob> savedJobs = savedContentService.getSavedJobs(jobSeekerId, pageable);
        Page<JobSummaryDto> dtos = savedJobs.map(jobMapper::toJobSummaryDto);
        return ResponseEntity.ok(dtos);
    }

    // --- Endpoints for Companies to manage saved seekers ---
    @CrossOrigin(originPatterns = "*")
    @PostMapping("{companyId}/seekers/{seekerId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<String> saveSeekerProfile(@PathVariable Long companyId, @PathVariable Long seekerId) {
        savedContentService.saveSeekerProfile(seekerId, companyId);
        return ResponseEntity.ok("Job seeker profile saved successfully");
    }
    @CrossOrigin(originPatterns = "*")
@DeleteMapping("{companyId}/seekers/{seekerId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Void> unsaveSeekerProfile(@PathVariable Long companyId, @PathVariable Long seekerId) {
        savedContentService.unsaveSeekerProfile(seekerId, companyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{companyId}/seekers")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Page<JobSeekerProfileDto>> getSavedSeekerProfiles(@PathVariable Long companyId, Pageable pageable) {
        Page<JobSeekerProfile> savedProfiles = savedContentService.getSavedSeekerProfiles(companyId, pageable);
        Page<JobSeekerProfileDto> dtos = savedProfiles.map(profileMapper::toJobSeekerProfileDto);
        return ResponseEntity.ok(dtos);
    }

    // --- Endpoints for Companies to manage saved job applications ---
    @CrossOrigin(originPatterns = "*")
    @PostMapping("{companyId}/applications/{applicationId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<String> saveJobApplication(@PathVariable Long companyId, @PathVariable Long applicationId) {
        savedContentService.saveJobApplication(applicationId, companyId);
        return ResponseEntity.ok("Job application saved successfully");
    }
    
    @CrossOrigin(originPatterns = "*")
@DeleteMapping("{companyId}/applications/{applicationId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Void> unsaveJobApplication(@PathVariable Long companyId, @PathVariable Long applicationId) {
        savedContentService.unsaveJobApplication(applicationId, companyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{companyId}/applications")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Page<SavedJobApplicationDto>> getSavedJobApplications(@PathVariable Long companyId, Pageable pageable) {
        Page<SavedJobApplication> savedApplications = savedContentService.getSavedJobApplications(companyId, pageable);
        Page<SavedJobApplicationDto> dtos = savedApplications.map(savedJobApplicationMapper::toSavedJobApplicationDto);
        return ResponseEntity.ok(dtos);
    }

    // --- Endpoints for Customers to manage saved businesses ---
    @CrossOrigin(originPatterns = "*")
    @PostMapping("{customerId}/businesses/{businessId}")
    public ResponseEntity<String> saveBusiness(@PathVariable Long customerId, @PathVariable Long businessId) {
        savedContentService.saveBusiness(businessId, customerId);
        return ResponseEntity.ok("Business saved successfully");
    }

    @CrossOrigin(originPatterns = "*")
@DeleteMapping("{customerId}/businesses/{businessId}")
    public ResponseEntity<Void> unsaveBusiness(@PathVariable Long customerId, @PathVariable Long businessId) {
        savedContentService.unsaveBusiness(businessId, customerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{customerId}/businesses")
    public ResponseEntity<Page<SavedBusinessSummaryDto>> getSavedBusinesses(@PathVariable Long customerId, Pageable pageable) {
        Page<SavedBusiness> savedBusinesses = savedContentService.getSavedBusinesses(customerId, pageable);
        Page<SavedBusinessSummaryDto> dtos = savedBusinesses.map(SavedBusinessSummaryDto::new);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("{customerId}/businesses/{businessId}/is-saved")
    public ResponseEntity<Boolean> isBusinessSaved(@PathVariable Long customerId, @PathVariable Long businessId) {
        boolean isSaved = savedContentService.isBusinessSaved(businessId, customerId);
        return ResponseEntity.ok(isSaved);
    }
}
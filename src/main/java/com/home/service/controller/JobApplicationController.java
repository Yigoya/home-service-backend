package com.home.service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.home.service.Service.JobApplicationService;
import com.home.service.dto.ApplicationDto;
import com.home.service.dto.ApplicationRequestDto;
import com.home.service.dto.JobMapper;
import com.home.service.dto.MyApplicationDto;
import com.home.service.models.JobApplication;
import com.home.service.models.enums.ApplicationStatus;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(originPatterns = "*")
@RestController 
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService applicationService;
    private final JobMapper jobMapper;

    @CrossOrigin(originPatterns = "*")
    @PostMapping("/jobs/{jobId}/apply")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ApplicationDto> applyForJob(@PathVariable Long jobId, @RequestBody ApplicationRequestDto requestDto) {
        JobApplication application = applicationService.applyToJob(jobId, requestDto, requestDto.getUserId());
        return new ResponseEntity<>(jobMapper.toApplicationDto(application), HttpStatus.CREATED);
    }
    @CrossOrigin(originPatterns = "*")
    @GetMapping("/jobs/{userId}/applications/{jobId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<List<ApplicationDto>> getApplicationsForJob(@PathVariable Long jobId, @PathVariable Long userId) {
        List<JobApplication> applications = applicationService.getApplicationsForJob(jobId, userId);
        List<ApplicationDto> dtos = applications.stream()
                .map(jobMapper::toApplicationDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    @CrossOrigin(originPatterns = "*")
    @GetMapping("/my-applications/{userId}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<Page<MyApplicationDto>> getMyApplications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "applicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<JobApplication> applications = applicationService.getApplicationsBySeeker(userId, pageable);
        Page<MyApplicationDto> dtos = applications.map(jobMapper::toMyApplicationDto);
        
        return ResponseEntity.ok(dtos);
    }

    @CrossOrigin(originPatterns = "*")
    @GetMapping("/companies/{companyId}/applications")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Page<ApplicationDto>> getApplicationsByCompany(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "applicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) Integer rating) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<JobApplication> applications = applicationService.getApplicationsByCompany(
            companyId, status, jobTitle, rating, pageable);
        
        Page<ApplicationDto> dtos = applications.map(jobMapper::toApplicationDto);
        
        return ResponseEntity.ok(dtos);
    }

    @CrossOrigin(originPatterns = "*")
    @GetMapping("/companies/{companyId}/applications/{applicationId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<ApplicationDto> getSingleApplication(
            @PathVariable Long companyId,
            @PathVariable Long applicationId) {
        JobApplication application = applicationService.getApplicationForCompanyWithAutoStatus(companyId, applicationId);
        return ResponseEntity.ok(jobMapper.toApplicationDto(application));
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/companies/{companyId}/applications/{applicationId}/status")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<ApplicationDto> updateApplicationStatus(
            @PathVariable Long companyId,
            @PathVariable Long applicationId,
            @RequestParam ApplicationStatus status) {
        JobApplication updated = applicationService.updateApplicationStatus(companyId, applicationId, status);
        return ResponseEntity.ok(jobMapper.toApplicationDto(updated));
    }
}
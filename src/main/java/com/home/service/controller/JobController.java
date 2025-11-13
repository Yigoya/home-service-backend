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

import com.home.service.Service.JobService;
import com.home.service.dto.CompanyJobDto;
import com.home.service.dto.CreateJobDto;
import com.home.service.dto.JobDetailDto;
import com.home.service.dto.JobMapper;
import com.home.service.dto.JobSummaryDto;
import com.home.service.models.Job;
import com.home.service.models.enums.JobType;
import com.home.service.models.enums.JobStatus;

import java.util.List;

@CrossOrigin(originPatterns = "*")
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final JobMapper jobMapper;

    @CrossOrigin(originPatterns = "*")
    @GetMapping
    public Page<JobSummaryDto> searchJobs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<JobType> jobTypes,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String postedDate,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(defaultValue = "Newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Sort sort = switch (sortBy) {
            case "Oldest" -> Sort.by("postedDate").ascending();
            case "Salary: High to Low" -> Sort.by("salaryMax").descending();
            case "Salary: Low to High" -> Sort.by("salaryMin").ascending();
            default -> Sort.by("postedDate").descending(); // "Newest" and "Relevance" default to this
        };

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Job> jobPage = jobService.searchJobs(category, jobTypes, location, keyword, level, postedDate, status, pageable);
        return jobPage.map(jobMapper::toJobSummaryDto);
    }

    @CrossOrigin(originPatterns = "*")
    @GetMapping("/{id}")
    public ResponseEntity<JobDetailDto> getJobById(@PathVariable Long id) {
        Job job = jobService.getJobById(id);
        List<Job> relatedJobs = jobService.getRelatedJobs(id, 
            job.getCompany().getId(), 
            job.getService().getId());
        return ResponseEntity.ok(jobMapper.toJobDetailDto(job, relatedJobs));
    }

    @CrossOrigin(originPatterns = "*")
    @PostMapping
    // @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<JobDetailDto> createJob(@RequestBody CreateJobDto createJobDto) {
        Job createdJob = jobService.createJob(createJobDto);
        List<Job> relatedJobs = jobService.getRelatedJobs(createdJob.getId(), 
            createdJob.getCompany().getId(), 
            createdJob.getService().getId());
        return new ResponseEntity<>(jobMapper.toJobDetailDto(createdJob, relatedJobs), HttpStatus.CREATED);
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<JobDetailDto> updateJob(@PathVariable Long id, @RequestBody CreateJobDto createJobDto) {
        Job updatedJob = jobService.updateJob(id, createJobDto);
        List<Job> relatedJobs = jobService.getRelatedJobs(id, 
            updatedJob.getCompany().getId(), 
            updatedJob.getService().getId());
        return ResponseEntity.ok(jobMapper.toJobDetailDto(updatedJob, relatedJobs));
    }

    @CrossOrigin(originPatterns = "*")
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    @CrossOrigin(originPatterns = "*")
    @GetMapping("/company/{companyId}")
    public ResponseEntity<Page<CompanyJobDto>> getJobsByCompanyId(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "Newest") String sortBy,
            @RequestParam(required = false) JobStatus status) {
        
        Sort sort = switch (sortBy) {
            case "Oldest" -> Sort.by("postedDate").ascending();
            case "Salary: High to Low" -> Sort.by("salaryMax").descending();
            case "Salary: Low to High" -> Sort.by("salaryMin").ascending();
            default -> Sort.by("postedDate").descending(); // "Newest" default
        };

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CompanyJobDto> companyJobs = jobService.getCompanyJobsWithApplicationCount(companyId, jobMapper, pageable, status);
        return ResponseEntity.ok(companyJobs);
    }
}
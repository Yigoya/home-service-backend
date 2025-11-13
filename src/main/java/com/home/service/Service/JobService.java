package com.home.service.Service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.home.service.config.exceptions.ResourceNotFoundException;
import com.home.service.dto.CreateJobDto;
import com.home.service.dto.CompanyJobDto;
import com.home.service.dto.JobMapper;
import com.home.service.models.CompanyProfile;
import com.home.service.models.Job;
import com.home.service.models.ServiceTranslation;
import com.home.service.models.Services;
import com.home.service.models.User;
import com.home.service.models.enums.EthiopianLanguage;
import com.home.service.models.enums.JobType;
import com.home.service.models.enums.JobStatus;
import com.home.service.repositories.CompanyProfileRepository;
import com.home.service.repositories.JobRepository;
import com.home.service.repositories.ServiceRepository;
import com.home.service.repositories.UserRepository;

import jakarta.persistence.criteria.Join;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
     private final ServiceRepository servicesRepository; 
    private final CompanyProfileRepository companyProfileRepository;
    private final com.home.service.repositories.JobApplicationRepository jobApplicationRepository;

    @Transactional
    public Page<Job> searchJobs(String category, List<JobType> jobTypes, String location, String keyword, String level, String postedDateFilter, JobStatus status, Pageable pageable) {
        Specification<Job> spec = (root, query, cb) -> {
            
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("company");
                root.fetch("service").fetch("translations");
            }
            
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isEmpty()) {
                String keywordLower = "%" + keyword.toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), keywordLower);
                Predicate descriptionLike = cb.like(cb.lower(root.get("description").as(String.class)), keywordLower);
                predicates.add(cb.or(titleLike, descriptionLike));
            }

            if (category != null && !category.isEmpty()) {
                Join<Services, ServiceTranslation> translationJoin = root.join("service").join("translations");
                predicates.add(cb.like(cb.lower(translationJoin.get("name")), "%" + category.toLowerCase() + "%"));
                predicates.add(cb.equal(translationJoin.get("lang"), EthiopianLanguage.ENGLISH));
            }
            if (jobTypes != null && !jobTypes.isEmpty()) {
                predicates.add(root.get("jobType").in(jobTypes));
            }
            if (location != null && !location.isEmpty()) {
                // Search in both company location and job location
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("jobLocation")), "%" + location.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("company").get("companyLocation")), "%" + location.toLowerCase() + "%")
                ));
            }
            
            if (level != null && !level.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("level")), "%" + level.toLowerCase() + "%"));
            }
            
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            System.out.println("Posted Date Filter: " + postedDateFilter);
            if (postedDateFilter != null && !postedDateFilter.isEmpty()) {
                try {
                    // Parse the date string in YYYY-MM-DD format
                    LocalDate filterDate = LocalDate.parse(postedDateFilter, DateTimeFormatter.ISO_LOCAL_DATE);
                    
                    // Convert to Instant for the end of the specified day (23:59:59.999)
                    Instant endOfDay = filterDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().minus(1, ChronoUnit.MILLIS);
                    
                    // Filter jobs posted up until the specified date (inclusive)
                    predicates.add(cb.greaterThanOrEqualTo(root.get("postedDate"), endOfDay));
                } catch (DateTimeParseException e) {
                    // If date parsing fails, ignore the filter
                    // Could also log the error or throw a custom exception
                }
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return jobRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", id));
    }

    @Transactional(readOnly = true)
    public List<Job> getRelatedJobs(Long jobId, Long companyId, Long serviceId) {
        // Get jobs from the same company or same service category, excluding the current job
        Specification<Job> spec = (root, query, cb) -> {
            root.fetch("company");
            root.fetch("service").fetch("translations");
            
            List<Predicate> predicates = new ArrayList<>();
            
            // Exclude the current job
            predicates.add(cb.notEqual(root.get("id"), jobId));
            
            // Jobs from same company OR same service category
            Predicate sameCompany = cb.equal(root.get("company").get("id"), companyId);
            Predicate sameService = cb.equal(root.get("service").get("id"), serviceId);
            predicates.add(cb.or(sameCompany, sameService));
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        // Limit to 3 related jobs
        return jobRepository.findAll(spec).stream().limit(3).toList();
    }

    @Transactional(readOnly = true)
    public List<Job> getJobsByCompanyId(Long companyId) {
        return jobRepository.findJobsByCompanyId(companyId);
    }

    @Transactional(readOnly = true)
    public Page<CompanyJobDto> getCompanyJobsWithApplicationCount(Long companyId, JobMapper jobMapper, Pageable pageable, JobStatus status) {
        Specification<Job> spec = (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("company");
                root.fetch("service").fetch("translations");
            }
            
            List<Predicate> predicates = new ArrayList<>();
            
            // Filter by company ID
            predicates.add(cb.equal(root.get("company").get("id"), companyId));
            
            // Filter by status if provided
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<Job> jobPage = jobRepository.findAll(spec, pageable);
        return jobPage.map(job -> {
            Long applicationCount = Long.valueOf(jobApplicationRepository.countApplicationsByJobId(job.getId()));
            return jobMapper.toCompanyJobDto(job, applicationCount);
        });
    }

    @Transactional
    public Job createJob(CreateJobDto jobDto) {
        CompanyProfile company = companyProfileRepository.findById(jobDto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("CompanyProfile", "id", jobDto.getCompanyId()));

        Services service = servicesRepository.findById(jobDto.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", jobDto.getServiceId()));

        Job job = new Job();
        job.setService(service);
        job.setCompany(company);
        job.setTitle(jobDto.getTitle());
        job.setDescription(jobDto.getDescription());
        job.setJobLocation(jobDto.getJobLocation());
        job.setJobType(JobType.valueOf(jobDto.getJobType()));
        job.setSalaryMin(jobDto.getSalaryMin());
        job.setSalaryMax(jobDto.getSalaryMax());
        job.setSalaryCurrency(jobDto.getSalaryCurrency());
        job.setResponsibilities(jobDto.getResponsibilities());
        job.setQualifications(jobDto.getQualifications());
        job.setBenefits(jobDto.getBenefits());
        job.setTags(jobDto.getTags());
        job.setLevel(jobDto.getLevel());
        job.setApplicationDeadline(jobDto.getApplicationDeadline());
        job.setContactEmail(jobDto.getContactEmail());
        job.setContactPhone(jobDto.getContactPhone());

        return jobRepository.save(job);
    }

    @Transactional
    public Job updateJob(Long jobId, CreateJobDto jobDto) {
        Job jobToUpdate = getJobById(jobId);
        // verifyJobOwnership(jobToUpdate, companyEmail);

        Services service = servicesRepository.findById(jobDto.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", jobDto.getServiceId()));
        
        jobToUpdate.setService(service);
        jobToUpdate.setTitle(jobDto.getTitle());
        jobToUpdate.setDescription(jobDto.getDescription());
        jobToUpdate.setJobLocation(jobDto.getJobLocation());
        jobToUpdate.setJobType(JobType.valueOf(jobDto.getJobType()));
        jobToUpdate.setSalaryMin(jobDto.getSalaryMin());
        jobToUpdate.setSalaryMax(jobDto.getSalaryMax());
        jobToUpdate.setSalaryCurrency(jobDto.getSalaryCurrency());
        jobToUpdate.setResponsibilities(jobDto.getResponsibilities());
        jobToUpdate.setQualifications(jobDto.getQualifications());
        jobToUpdate.setBenefits(jobDto.getBenefits());
        jobToUpdate.setTags(jobDto.getTags());
        jobToUpdate.setLevel(jobDto.getLevel());
        jobToUpdate.setApplicationDeadline(jobDto.getApplicationDeadline());
        jobToUpdate.setContactEmail(jobDto.getContactEmail());
        jobToUpdate.setContactPhone(jobDto.getContactPhone());

        return jobRepository.save(jobToUpdate);
    }

    @Transactional
    public void deleteJob(Long jobId) {
        Job jobToDelete = getJobById(jobId);
        // verifyJobOwnership(jobToDelete, companyEmail);
        jobRepository.delete(jobToDelete);
    }

    private void verifyJobOwnership(Job job, Long companyId) {
        if (!job.getCompany().getId().equals(companyId)) {
            throw new AccessDeniedException("You do not have permission to modify this job.");
        }
    }
}
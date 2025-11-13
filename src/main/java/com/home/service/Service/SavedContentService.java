package com.home.service.Service;

import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.home.service.config.exceptions.ResourceNotFoundException;
import com.home.service.models.Business;
import com.home.service.models.Customer;
import com.home.service.models.Job;
import com.home.service.models.JobApplication;
import com.home.service.models.JobSeekerProfile;
import com.home.service.models.SavedBusiness;
import com.home.service.models.SavedJob;
import com.home.service.models.SavedJobApplication;
import com.home.service.models.SavedSeekerProfile;
import com.home.service.models.User;
import com.home.service.models.enums.ApplicationStatus;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.CustomerRepository;
import com.home.service.repositories.JobApplicationRepository;
import com.home.service.repositories.JobRepository;
import com.home.service.repositories.JobSeekerProfileRepository;
import com.home.service.repositories.SavedBusinessRepository;
import com.home.service.repositories.SavedJobApplicationRepository;
import com.home.service.repositories.SavedJobRepository;
import com.home.service.repositories.SavedSeekerProfileRepository;
import com.home.service.repositories.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class SavedContentService {

    private final SavedJobRepository savedJobRepository;
    private final SavedSeekerProfileRepository savedSeekerProfileRepository;
    private final SavedJobApplicationRepository savedJobApplicationRepository;
    private final SavedBusinessRepository savedBusinessRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final BusinessRepository businessRepository;
    private final CustomerRepository customerRepository;

    // ============== SAVED JOBS LOGIC (for Job Seekers) ==============

    @Transactional
    public void saveJob(Long jobId, Long jobSeekerId) {
        User jobSeeker = findUserById(jobSeekerId);
        if (savedJobRepository.existsByJobSeekerAndJobId(jobSeeker, jobId)) {
            throw new EntityExistsException("Job is already saved.");
        }
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
        
        SavedJob savedJob = new SavedJob(jobSeeker, job);
        savedJobRepository.save(savedJob);
    }

    @Transactional
    public void unsaveJob(Long jobId, Long jobSeekerId) {
        User jobSeeker = findUserById(jobSeekerId);
        SavedJob savedJob = savedJobRepository.findByJobSeekerAndJobId(jobSeeker, jobId)
                .orElseThrow(() -> new ResourceNotFoundException("SavedJob", "jobId", jobId));
        savedJobRepository.delete(savedJob);
    }

    @Transactional(readOnly = true)
    public Page<SavedJob> getSavedJobs(Long jobSeekerId, Pageable pageable) {
        User jobSeeker = findUserById(jobSeekerId);
        return savedJobRepository.findByJobSeeker(jobSeeker, pageable);
    }

    // ============== SAVED SEEKER PROFILES LOGIC (for Companies) ==============

    @Transactional
    public void saveSeekerProfile(Long seekerId, Long companyId) {
        User companyUser = findUserById(companyId);
        if (savedSeekerProfileRepository.existsByCompanyUserAndJobSeekerProfileId(companyUser, seekerId)) {
            throw new EntityExistsException("Job seeker is already saved.");
        }
        JobSeekerProfile seekerProfile = jobSeekerProfileRepository.findById(seekerId)
                .orElseThrow(() -> new ResourceNotFoundException("JobSeekerProfile", "id", seekerId));

        SavedSeekerProfile savedSeeker = new SavedSeekerProfile(companyUser, seekerProfile);
        savedSeekerProfileRepository.save(savedSeeker);
    }

    @Transactional
    public void unsaveSeekerProfile(Long seekerId, Long companyId) {
        User companyUser = findUserById(companyId);
        SavedSeekerProfile savedSeeker = savedSeekerProfileRepository.findByCompanyUserAndJobSeekerProfileId(companyUser, seekerId)
                .orElseThrow(() -> new ResourceNotFoundException("SavedSeekerProfile", "seekerId", seekerId));
        savedSeekerProfileRepository.delete(savedSeeker);
    }

    @Transactional(readOnly = true)
    public Page<JobSeekerProfile> getSavedSeekerProfiles(Long companyId, Pageable pageable) {
        User companyUser = findUserById(companyId);
        return savedSeekerProfileRepository.findByCompanyUser(companyUser, pageable)
                .map(SavedSeekerProfile::getJobSeekerProfile);
    }

    // ============== SAVED JOB APPLICATIONS LOGIC (for Companies) ==============

    @Transactional
    public void saveJobApplication(Long applicationId, Long companyId) {
        User companyUser = findUserById(companyId);
        if (savedJobApplicationRepository.existsByCompanyUserAndJobApplicationId(companyUser, applicationId)) {
            throw new EntityExistsException("Job application is already saved.");
        }
        JobApplication jobApplication = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", "id", applicationId));

        SavedJobApplication savedApplication = new SavedJobApplication(companyUser, jobApplication);
        savedJobApplicationRepository.save(savedApplication);

        // When a company saves an application, mark it as SHORTLISTED if it belongs to the company
        // and it's not already in a terminal state
        try {
            Long applicationCompanyId = jobApplication.getJob().getCompany().getId();
            if (applicationCompanyId != null && applicationCompanyId.equals(companyId)) {
                if (jobApplication.getStatus() != ApplicationStatus.HIRED &&
                    jobApplication.getStatus() != ApplicationStatus.REJECTED &&
                    jobApplication.getStatus() != ApplicationStatus.SHORTLISTED) {
                    jobApplication.setStatus(ApplicationStatus.SHORTLISTED);
                    jobApplicationRepository.save(jobApplication);
                }
            }
        } catch (Exception ignored) {
            // Swallow to avoid failing the save action if status update has an issue
        }
    }

    @Transactional
    public void unsaveJobApplication(Long applicationId, Long companyId) {
        User companyUser = findUserById(companyId);
        SavedJobApplication savedApplication = savedJobApplicationRepository.findByCompanyUserAndJobApplicationId(companyUser, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("SavedJobApplication", "applicationId", applicationId));
        savedJobApplicationRepository.delete(savedApplication);
    }

    @Transactional(readOnly = true)
    public Page<SavedJobApplication> getSavedJobApplications(Long companyId, Pageable pageable) {
        User companyUser = findUserById(companyId);
        return savedJobApplicationRepository.findByCompanyUser(companyUser, pageable);
    }

    // ============== SAVED BUSINESSES LOGIC (for Customers) ==============

    @Transactional
    public void saveBusiness(Long businessId, Long customerId) {
        Customer customer = findCustomerById(customerId);
        if (savedBusinessRepository.existsByCustomerAndBusinessId(customer, businessId)) {
            throw new EntityExistsException("Business is already saved.");
        }
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", "id", businessId));
        
        SavedBusiness savedBusiness = new SavedBusiness(customer, business);
        savedBusinessRepository.save(savedBusiness);
    }

    @Transactional
    public void unsaveBusiness(Long businessId, Long customerId) {
        Customer customer = findCustomerById(customerId);
        SavedBusiness savedBusiness = savedBusinessRepository.findByCustomerAndBusinessId(customer, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("SavedBusiness", "businessId", businessId));
        savedBusinessRepository.delete(savedBusiness);
    }

    @Transactional(readOnly = true)
    public Page<SavedBusiness> getSavedBusinesses(Long customerId, Pageable pageable) {
        Customer customer = findCustomerById(customerId);
        return savedBusinessRepository.findByCustomer(customer, pageable);
    }

    @Transactional(readOnly = true)
    public boolean isBusinessSaved(Long businessId, Long customerId) {
        Customer customer = findCustomerById(customerId);
        return savedBusinessRepository.existsByCustomerAndBusinessId(customer, businessId);
    }
    
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private Customer findCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
    }
}
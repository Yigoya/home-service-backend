package com.home.service.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.home.service.models.JobApplication;
import com.home.service.models.enums.ApplicationStatus;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    /**
     * Checks if an application exists for a specific job and job seeker.
     * This is used to prevent duplicate applications.
     * @param jobId The ID of the job.
     * @param jobSeekerId The ID of the job seeker (User).
     * @return true if an application exists, false otherwise.
     */
    boolean existsByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId);

    /**
     * Finds all applications submitted for a specific job.
     * This is used by companies to view candidates.
     * @param jobId The ID of the job.
     * @return A list of job applications for the given job.
     */
    List<JobApplication> findByJobId(Long jobId);

    /**
     * Finds all applications submitted by a single job seeker.
     * This is used by job seekers to track their application history.
     * @param jobSeekerId The ID of the job seeker (User).
     * @return A list of job applications submitted by the user.
     */
    List<JobApplication> findByJobSeekerId(Long jobSeekerId);

    /**
     * Finds all applications submitted by a single job seeker with pagination.
     * This is used by job seekers to track their application history with pagination support.
     * @param jobSeekerId The ID of the job seeker (User).
     * @param pageable Pagination information.
     * @return A page of job applications submitted by the user.
     */
    Page<JobApplication> findByJobSeekerId(Long jobSeekerId, Pageable pageable);

    /**
     * Count applications for a specific job
     */
    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.job.id = :jobId")
    Integer countApplicationsByJobId(@Param("jobId") Long jobId);

    /**
     * Count hired applications for a company in a specific year
     */
    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.job.company.id = :companyId AND ja.status = :status AND YEAR(ja.applicationDate) = :year")
    Integer countHiredApplicationsByCompanyAndYear(@Param("companyId") Long companyId, @Param("status") ApplicationStatus status, @Param("year") Integer year);

    /**
     * Count hired applications by department (service) for a company in a specific year
     */
    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.job.company.id = :companyId AND ja.status = :status AND YEAR(ja.applicationDate) = :year")
    List<Object[]> countHiredApplicationsByDepartmentAndYear(@Param("companyId") Long companyId, @Param("status") ApplicationStatus status, @Param("year") Integer year);

    /**
     * Count total hired applications for a company (all time)
     */
    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.job.company.id = :companyId AND ja.status = :status")
    Integer countTotalHiredApplicationsByCompany(@Param("companyId") Long companyId, @Param("status") ApplicationStatus status);

    /**
     * Find all applications for jobs posted by a specific company with pagination
     */
    @Query("SELECT ja FROM JobApplication ja WHERE ja.job.company.id = :companyId")
    Page<JobApplication> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    /**
     * Find applications for a company filtered by status with pagination
     */
    @Query("SELECT ja FROM JobApplication ja WHERE ja.job.company.id = :companyId AND ja.status = :status")
    Page<JobApplication> findByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") ApplicationStatus status, Pageable pageable);

    /**
     * Find applications for a company filtered by job title with pagination
     */
    @Query("SELECT ja FROM JobApplication ja WHERE ja.job.company.id = :companyId AND LOWER(ja.job.title) LIKE LOWER(CONCAT('%', :jobTitle, '%'))")
    Page<JobApplication> findByCompanyIdAndJobTitle(@Param("companyId") Long companyId, @Param("jobTitle") String jobTitle, Pageable pageable);

    /**
     * Find applications for a company filtered by status and job title with pagination
     */
    @Query("SELECT ja FROM JobApplication ja WHERE ja.job.company.id = :companyId AND ja.status = :status AND LOWER(ja.job.title) LIKE LOWER(CONCAT('%', :jobTitle, '%'))")
    Page<JobApplication> findByCompanyIdAndStatusAndJobTitle(@Param("companyId") Long companyId, @Param("status") ApplicationStatus status, @Param("jobTitle") String jobTitle, Pageable pageable);

    /**
     * Find applications for a company filtered by rating with pagination
     */
    @Query("SELECT ja FROM JobApplication ja WHERE ja.job.company.id = :companyId AND ja.rating = :rating")
    Page<JobApplication> findByCompanyIdAndRating(@Param("companyId") Long companyId, @Param("rating") Integer rating, Pageable pageable);
}
package com.home.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.home.service.models.Job;

import java.time.LocalDate;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    // JpaSpecificationExecutor allows for dynamic queries (filtering)
    
    /**
     * Find all active jobs for a company (jobs that haven't passed their deadline)
     */
    @Query("SELECT j FROM Job j WHERE j.company.id = :companyId AND (j.applicationDeadline IS NULL OR j.applicationDeadline >= :currentDate)")
    List<Job> findActiveJobsByCompanyId(@Param("companyId") Long companyId, @Param("currentDate") LocalDate currentDate);
    
    /**
     * Count active jobs for a company
     */
    @Query("SELECT COUNT(j) FROM Job j WHERE j.company.id = :companyId AND (j.applicationDeadline IS NULL OR j.applicationDeadline >= :currentDate)")
    Integer countActiveJobsByCompanyId(@Param("companyId") Long companyId, @Param("currentDate") LocalDate currentDate);
    
    /**
     * Find all jobs posted by a company in a specific year
     */
    @Query("SELECT j FROM Job j WHERE j.company.id = :companyId AND YEAR(j.postedDate) = :year")
    List<Job> findJobsByCompanyIdAndYear(@Param("companyId") Long companyId, @Param("year") Integer year);
    
    /**
     * Find all jobs by company ID with company and service data
     */
    @Query("SELECT j FROM Job j LEFT JOIN FETCH j.company LEFT JOIN FETCH j.service s LEFT JOIN FETCH s.translations WHERE j.company.id = :companyId")
    List<Job> findJobsByCompanyId(@Param("companyId") Long companyId);
}
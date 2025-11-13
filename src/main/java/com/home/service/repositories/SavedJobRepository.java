package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.SavedJob;
import com.home.service.models.User;

import java.util.List;
import java.util.Optional;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    
    // Find all jobs saved by a specific user
    List<SavedJob> findByJobSeeker(User jobSeeker);
    
    // Find all jobs saved by a specific user with pagination
    Page<SavedJob> findByJobSeeker(User jobSeeker, Pageable pageable);
    
    // Find a specific saved job entry to delete it
    Optional<SavedJob> findByJobSeekerAndJobId(User jobSeeker, Long jobId);
    
    // Check if a job is already saved to prevent duplicates
    boolean existsByJobSeekerAndJobId(User jobSeeker, Long jobId);
}
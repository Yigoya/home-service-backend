package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.SavedJobApplication;
import com.home.service.models.User;

import java.util.List;
import java.util.Optional;

public interface SavedJobApplicationRepository extends JpaRepository<SavedJobApplication, Long> {

    // Find all job applications saved by a specific company user
    List<SavedJobApplication> findByCompanyUser(User companyUser);
    
    // Find all job applications saved by a specific company user with pagination
    Page<SavedJobApplication> findByCompanyUser(User companyUser, Pageable pageable);

    // Find a specific saved job application entry to delete it
    Optional<SavedJobApplication> findByCompanyUserAndJobApplicationId(User companyUser, Long jobApplicationId);

    // Check if a job application is already saved
    boolean existsByCompanyUserAndJobApplicationId(User companyUser, Long jobApplicationId);
}
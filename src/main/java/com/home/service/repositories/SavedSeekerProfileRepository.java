package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.SavedSeekerProfile;
import com.home.service.models.User;

import java.util.List;
import java.util.Optional;

public interface SavedSeekerProfileRepository extends JpaRepository<SavedSeekerProfile, Long> {

    // Find all seeker profiles saved by a specific company user
    List<SavedSeekerProfile> findByCompanyUser(User companyUser);
    
    // Find all seeker profiles saved by a specific company user with pagination
    Page<SavedSeekerProfile> findByCompanyUser(User companyUser, Pageable pageable);

    // Find a specific saved profile entry to delete it
    Optional<SavedSeekerProfile> findByCompanyUserAndJobSeekerProfileId(User companyUser, Long jobSeekerProfileId);

    // Check if a profile is already saved
    boolean existsByCompanyUserAndJobSeekerProfileId(User companyUser, Long jobSeekerProfileId);
}
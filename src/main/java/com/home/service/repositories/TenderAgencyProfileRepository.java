package com.home.service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.home.service.models.TenderAgencyProfile;
import com.home.service.models.User;

@Repository
public interface TenderAgencyProfileRepository extends JpaRepository<TenderAgencyProfile, Long> {
    boolean existsByCompanyName(String companyName);

    Optional<TenderAgencyProfile> findByUser(User user);
}

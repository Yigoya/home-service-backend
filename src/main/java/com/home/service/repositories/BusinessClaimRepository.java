package com.home.service.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.Business;
import com.home.service.models.BusinessClaim;
import com.home.service.models.User;

public interface BusinessClaimRepository extends JpaRepository<BusinessClaim, Long> {
    List<BusinessClaim> findByBusiness(Business business);

    Page<BusinessClaim> findByUser(User user, Pageable pageable);
}
package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.Partnership;

public interface PartnershipRepository extends JpaRepository<Partnership, Long> {
    Page<Partnership> findByCompanyId(Long companyId, Pageable pageable);
}
package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.home.service.models.B2BPartner;
import com.home.service.models.enums.B2BPartnerStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface B2BPartnerRepository extends JpaRepository<B2BPartner, Long> {
    Page<B2BPartner> findByBusinessId(Long businessId, Pageable pageable);

    Page<B2BPartner> findByPartnerBusinessId(Long partnerBusinessId, Pageable pageable);

    List<B2BPartner> findByBusinessIdAndStatus(Long businessId, B2BPartnerStatus status);

    Optional<B2BPartner> findByBusinessIdAndPartnerBusinessId(Long businessId, Long partnerBusinessId);
}
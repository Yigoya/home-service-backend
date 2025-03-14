package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.home.service.models.Quote;
import com.home.service.models.enums.QuoteStatus;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    Page<Quote> findByBusinessId(Long businessId, Pageable pageable);

    Page<Quote> findByPartnerId(Long partnerId, Pageable pageable);

    Page<Quote> findByBusinessIdAndStatus(Long businessId, QuoteStatus status, Pageable pageable);
}
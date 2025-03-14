package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.home.service.models.Contract;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    Page<Contract> findByBusinessId(Long businessId, Pageable pageable);

    Page<Contract> findByPartnerId(Long partnerId, Pageable pageable);
}
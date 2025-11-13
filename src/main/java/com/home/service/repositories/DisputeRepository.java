package com.home.service.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.home.service.models.Dispute;
import com.home.service.models.enums.DisputeStatus;

public interface DisputeRepository extends JpaRepository<Dispute, Long>, JpaSpecificationExecutor<Dispute> {
    List<Dispute> findAllByStatus(DisputeStatus status);

    List<Dispute> findByCustomerId(Long customerId);
    
    Page<Dispute> findByCustomerId(Long customerId, Pageable pageable);
}

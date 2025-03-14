package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Page<Supplier> findByCompanyId(Long companyId, Pageable pageable);
}
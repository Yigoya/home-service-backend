package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.home.service.models.BusinessCustomer;

public interface BusinessCustomerRepository extends JpaRepository<BusinessCustomer, Long> {
    @Query("SELECT c FROM BusinessCustomer c WHERE c.company.id = :companyId AND " +
            "(:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<BusinessCustomer> findByCompanyId(Long companyId, String search, Pageable pageable);
}

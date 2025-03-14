package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.home.service.models.Business;
import com.home.service.models.BusinessServices;

public interface BusinessServiceRepository extends JpaRepository<BusinessServices, Long> {
    Page<BusinessServices> findByBusiness(Business business, Pageable pageable);

    @Query("SELECT s FROM BusinessServices s WHERE s.business.id = :companyId AND " +
            "(:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<BusinessServices> findByCompanyId(Long companyId, String search, Pageable pageable);
}
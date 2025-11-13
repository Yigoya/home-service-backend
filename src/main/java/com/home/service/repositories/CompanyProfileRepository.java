package com.home.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.home.service.dto.CompanySearchDto; 
import com.home.service.models.CompanyProfile;

public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {
    
    @Query(value = "SELECT * FROM company_profiles cp " +
           "WHERE (:name IS NULL OR LOWER(CAST(cp.company_name AS TEXT)) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:industry IS NULL OR LOWER(CAST(cp.industry AS TEXT)) LIKE LOWER(CONCAT('%', :industry, '%'))) " +
           "AND (:location IS NULL OR LOWER(CAST(cp.company_location AS TEXT)) LIKE LOWER(CONCAT('%', :location, '%')))", 
           nativeQuery = true)
    Page<CompanyProfile> searchCompanies(@Param("name") String name, 
                                       @Param("industry") String industry, 
                                       @Param("location") String location, 
                                       Pageable pageable);
}

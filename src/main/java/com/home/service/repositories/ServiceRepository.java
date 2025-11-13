package com.home.service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.home.service.models.ServiceCategory;
import com.home.service.models.Services;

public interface ServiceRepository extends JpaRepository<Services, Long> {
    List<Services> findByCategoryOrderByIdAsc(ServiceCategory category);

    // Returns services filtered by category ordered by displayOrder ascending
    @Query("SELECT s FROM Services s WHERE s.category = :category ORDER BY CASE WHEN s.displayOrder IS NULL THEN 1 ELSE 0 END, s.displayOrder ASC, s.id ASC")
    List<Services> findByCategoryOrderByDisplayOrderAsc(@Param("category") ServiceCategory category);

    

    // List<Services> findByCategoryAndServiceIdIsNull(ServiceCategory category);

    @Query("SELECT COUNT(t) FROM Technician t JOIN t.services s WHERE s.id = :serviceId")
    int countTechniciansByServiceId(@Param("serviceId") Long serviceId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.service.id = :serviceId")
    int countBookingsByServiceId(@Param("serviceId") Long serviceId);

    List<Services> findByAgency_IdOrderByIdAsc(Long agencyId);

    List<Services> findByServiceIdIsNullOrderByIdAsc();

    @Query("SELECT s FROM Services s JOIN s.services p WHERE p IS NOT NULL")
    List<Services> findServicesWithProducts();
}

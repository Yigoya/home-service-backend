package com.home.service.repositories;

import java.util.List;

import org.checkerframework.checker.units.qual.A;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.home.service.models.ServiceCategory;
import com.home.service.models.Services;

public interface ServiceRepository extends JpaRepository<Services, Long> {
    List<Services> findByCategory(ServiceCategory category);

    // List<Services> findByCategoryAndServiceIdIsNull(ServiceCategory category);

    @Query("SELECT COUNT(t) FROM Technician t JOIN t.services s WHERE s.id = :serviceId")
    int countTechniciansByServiceId(@Param("serviceId") Long serviceId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.service.id = :serviceId")
    int countBookingsByServiceId(@Param("serviceId") Long serviceId);

    List<Services> findByAgency_Id(Long agencyId);

    List<Services> findByServiceIdIsNull();

}

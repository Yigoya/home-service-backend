package com.home.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.home.service.models.AgencyBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AgencyBookingRepository
                extends JpaRepository<AgencyBooking, Long>, JpaSpecificationExecutor<AgencyBooking> {
        @Query("SELECT b FROM AgencyBooking b WHERE b.customer.id = :customerId ORDER BY " +
                        "CASE b.status " +
                        "WHEN 'PENDING' THEN 1 " +
                        "WHEN 'ACCEPTED' THEN 2 " +
                        "WHEN 'STARTED' THEN 3 " +
                        "WHEN 'DENIED' THEN 4 " +
                        "WHEN 'COMPLETED' THEN 5 " +
                        "WHEN 'CANCELED' THEN 6 " +
                        "END, b.scheduledDate ASC")
        Page<AgencyBooking> findByCustomerIdOrderByStatusPriority(@Param("customerId") Long customerId,
                        Pageable pageable);

        // @Query("SELECT b FROM AgencyBooking b WHERE b.technician.id = :technicianId
        // ORDER BY " +
        // "CASE b.status " +
        // "WHEN 'PENDING' THEN 1 " +
        // "WHEN 'ACCEPTED' THEN 2 " +
        // "WHEN 'STARTED' THEN 3 " +
        // "WHEN 'DENIED' THEN 4 " +
        // "WHEN 'COMPLETED' THEN 5 " +
        // "WHEN 'CANCELED' THEN 6 " +
        // "END, b.scheduledDate ASC")
        // Page<AgencyBooking>
        // findByTechnicianIdOrderByStatusPriority(@Param("technicianId") Long
        // technicianId,
        // Pageable pageable);

        // List<AgencyBooking> findAllByTechnician_Id(Long technicianId);

        List<AgencyBooking> findByCustomer_Id(Long customerId);

        List<AgencyBooking> findByAgency_Id(Long agencyId);
}

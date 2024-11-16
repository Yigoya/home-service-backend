package com.home.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.home.service.models.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {
        @Query("SELECT b FROM Booking b WHERE b.customer.id = :customerId ORDER BY " +
                        "CASE b.status " +
                        "WHEN 'PENDING' THEN 1 " +
                        "WHEN 'CONFIRMED' THEN 2 " +
                        "WHEN 'ACCEPTED' THEN 3 " +
                        "WHEN 'DENIED' THEN 4 " +
                        "WHEN 'COMPLETED' THEN 5 " +
                        "WHEN 'CANCELED' THEN 6 " +
                        "END, b.scheduledDate ASC")
        Page<Booking> findByCustomerIdOrderByStatusPriority(@Param("customerId") Long customerId, Pageable pageable);

        @Query("SELECT b FROM Booking b WHERE b.technician.id = :technicianId ORDER BY " +
                        "CASE b.status " +
                        "WHEN 'PENDING' THEN 1 " +
                        "WHEN 'CONFIRMED' THEN 2 " +
                        "WHEN 'ACCEPTED' THEN 3 " +
                        "WHEN 'DENIED' THEN 4 " +
                        "WHEN 'COMPLETED' THEN 5 " +
                        "WHEN 'CANCELED' THEN 6 " +
                        "END, b.scheduledDate ASC")
        Page<Booking> findByTechnicianIdOrderByStatusPriority(@Param("technicianId") Long technicianId,
                        Pageable pageable);

        List<Booking> findAllByTechnician_Id(Long technicianId);

        List<Booking> findByCustomer_Id(Long customerId);
}

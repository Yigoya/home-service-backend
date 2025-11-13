package com.home.service.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.home.service.models.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
       // Prefer this method to avoid NonUniqueResultException when multiple reviews exist for a booking
       Optional<Review> findFirstByBookingIdOrderByCreatedAtDesc(Long bookingId);
    
       // Kept for backward compatibility in case it's used elsewhere; consider removing after migration
       // Optional<Review> findByBookingId(Long bookingId);

    List<Review> findAllByTechnicianId(Long technicianId);

    @Query("SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.customer c " +
           "LEFT JOIN FETCH c.user cu " +
           "LEFT JOIN FETCH r.booking b " +
           "LEFT JOIN FETCH b.service s " +
           "LEFT JOIN FETCH b.serviceLocation sl " +
           "WHERE r.technician.id = :technicianId " +
           "ORDER BY r.createdAt DESC")
    List<Review> findAllByTechnicianIdWithDetails(@Param("technicianId") Long technicianId);

    @Query(value = "SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.customer c " +
           "LEFT JOIN FETCH c.user cu " +
           "LEFT JOIN FETCH r.booking b " +
           "LEFT JOIN FETCH b.service s " +
           "LEFT JOIN FETCH b.serviceLocation sl " +
           "WHERE r.technician.id = :technicianId",
           countQuery = "SELECT COUNT(r) FROM Review r WHERE r.technician.id = :technicianId")
    Page<Review> findAllByTechnicianIdWithDetails(@Param("technicianId") Long technicianId, Pageable pageable);

    List<Review> findTop5ByOrderByRatingDesc();
}

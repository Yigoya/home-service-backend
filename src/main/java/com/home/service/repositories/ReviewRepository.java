package com.home.service.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByBookingId(Long bookingId);

    List<Review> findAllByTechnicianId(Long technicianId);

    List<Review> findTop5ByOrderByRatingDesc();
}

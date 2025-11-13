package com.home.service.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.home.service.dto.ReviewDTO;
import com.home.service.dto.ReviewRequest;
import com.home.service.dto.TechnicianReviewDTO;
import com.home.service.models.Booking;
import com.home.service.models.Review;
import com.home.service.models.Technician;
import com.home.service.repositories.BookingRepository;
import com.home.service.repositories.ReviewRepository;
import com.home.service.repositories.TechnicianRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ReviewService {

        private final ReviewRepository reviewRepository;
        private final BookingRepository bookingRepository;
        private final TechnicianRepository technicianRepository;

        public ReviewService(ReviewRepository reviewRepository, BookingRepository bookingRepository,
                        TechnicianRepository technicianRepository) {
                this.reviewRepository = reviewRepository;
                this.bookingRepository = bookingRepository;
                this.technicianRepository = technicianRepository;
        }

        public void submitReview(ReviewRequest reviewRequest) {
                // Fetch the booking using the provided bookingId
                Booking booking = bookingRepository.findById(reviewRequest.getBookingId())
                                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

                // A booking should have at most one review. Previously the code attempted to
                // look up a review using businessId (which is not the booking id) leading to
                // always creating a new Review and multiple rows per booking. That resulted in
                // NonUniqueResultException when calling findByBookingId(). Correct to use
                // bookingId here.
                Review review = reviewRepository.findFirstByBookingIdOrderByCreatedAtDesc(reviewRequest.getBookingId())
                                .orElse(new Review());

                review.setBooking(booking);
                review.setCustomer(booking.getCustomer());
                review.setTechnician(booking.getTechnician());
                review.setRating(reviewRequest.getRating());
                review.setReviewText(reviewRequest.getComment());

                reviewRepository.save(review);

                // Recalculate and update technician rating
                updateTechnicianRating(booking.getTechnician().getId());
        }

        private void updateTechnicianRating(Long technicianId) {
                List<Review> technicianReviews = reviewRepository.findAllByTechnicianId(technicianId);
                double averageRating = technicianReviews.stream()
                                .mapToInt(Review::getRating)
                                .average()
                                .orElse(0.0);

                Technician technician = technicianRepository.findById(technicianId)
                                .orElseThrow(() -> new IllegalArgumentException("Technician not found"));
                technician.setRating(averageRating);

                technicianRepository.save(technician);
        }

        public List<ReviewDTO> getTop5ReviewsByRating() {
                List<Review> reviews = reviewRepository.findTop5ByOrderByRatingDesc();
                return reviews.stream()
                                .map(review -> new ReviewDTO(review))
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public List<TechnicianReviewDTO> getReviewsByTechnicianId(Long technicianId) {
                List<Review> reviews = reviewRepository.findAllByTechnicianIdWithDetails(technicianId);
                return reviews.stream()
                                .map(TechnicianReviewDTO::new)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public Page<TechnicianReviewDTO> getReviewsByTechnicianId(Long technicianId, int page, int size, String sortBy, String sortDirection) {
                Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
                
                Page<Review> reviewPage = reviewRepository.findAllByTechnicianIdWithDetails(technicianId, pageable);
                return reviewPage.map(TechnicianReviewDTO::new);
        }
}

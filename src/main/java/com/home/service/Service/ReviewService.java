package com.home.service.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.home.service.dto.ReviewDTO;
import com.home.service.dto.ReviewRequest;
import com.home.service.models.Booking;
import com.home.service.models.Review;
import com.home.service.models.Technician;
import com.home.service.repositories.BookingRepository;
import com.home.service.repositories.ReviewRepository;
import com.home.service.repositories.TechnicianRepository;

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
        Booking booking = bookingRepository.findById(reviewRequest.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        Review review = reviewRepository.findByBookingId(reviewRequest.getBookingId())
                .orElse(new Review());

        review.setBooking(booking);
        review.setCustomer(booking.getCustomer());
        review.setTechnician(booking.getTechnician());
        review.setRating(reviewRequest.getRating());
        review.setReviewText(reviewRequest.getReview());

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
}

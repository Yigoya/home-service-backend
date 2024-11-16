package com.home.service.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.home.service.dto.ReviewDTO;
import com.home.service.dto.ReviewRequest;
import com.home.service.models.Booking;
import com.home.service.models.Review;
import com.home.service.repositories.BookingRepository;
import com.home.service.repositories.ReviewRepository;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    public ReviewService(ReviewRepository reviewRepository, BookingRepository bookingRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
    }

    public void submitReview(ReviewRequest reviewRequest) {
        Booking booking = bookingRepository.findById(reviewRequest.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        Review review = new Review();
        review.setBooking(booking);
        review.setCustomer(booking.getCustomer());
        review.setTechnician(booking.getTechnician());
        review.setRating(reviewRequest.getRating());
        review.setReviewText(reviewRequest.getReview());

        reviewRepository.save(review);
    }

    public List<ReviewDTO> getTop5ReviewsByRating() {
        List<Review> reviews = reviewRepository.findTop5ByOrderByRatingDesc();
        return reviews.stream()
                .map(review -> new ReviewDTO(review))
                .collect(Collectors.toList());
    }
}

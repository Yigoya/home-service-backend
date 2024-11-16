package com.home.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.home.service.models.Review;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private Long bookingId;
    private Long customerId;
    private Long technicianId;
    private Integer rating;
    private String review;
    private CustomerProfileDTO customer;

    public ReviewDTO(Review review) {

        // Initialize fields using the review object
        this.id = review.getId();
        this.bookingId = review.getBooking().getId();
        this.customerId = review.getCustomer().getId();
        this.technicianId = review.getTechnician().getId();
        this.rating = review.getRating();
        this.review = review.getReviewText();
        this.customer = new CustomerProfileDTO(review.getCustomer());

    }
}

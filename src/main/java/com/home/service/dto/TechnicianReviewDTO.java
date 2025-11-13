package com.home.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.home.service.models.Review;
import com.home.service.models.enums.BookingStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianReviewDTO {
    private Long reviewId;
    private Integer rating;
    private String reviewText;
    private LocalDateTime createdAt;
    
    // Customer information
    private CustomerInfo customer;
    
    // Booking information
    private BookingInfo booking;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        private Long customerId;
        private String customerName;
        private String customerEmail;
        private String customerPhone;
        private String customerProfileImage;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingInfo {
        private Long bookingId;
        private String serviceName;
        private LocalDateTime scheduledDate;
        private String timeSchedule;
        private BookingStatus status;
        private Double totalCost;
        private String description;
        private String serviceLocationAddress;
    }
    
    public TechnicianReviewDTO(Review review) {
        this.reviewId = review.getId();
        this.rating = review.getRating();
        this.reviewText = review.getReviewText();
        this.createdAt = review.getCreatedAt();
        
        // Map customer information
        if (review.getCustomer() != null && review.getCustomer().getUser() != null) {
            this.customer = new CustomerInfo(
                review.getCustomer().getId(),
                review.getCustomer().getUser().getName(),
                review.getCustomer().getUser().getEmail(),
                review.getCustomer().getUser().getPhoneNumber(),
                review.getCustomer().getUser().getProfileImage()
            );
        }
        
        // Map booking information
        if (review.getBooking() != null) {
            String serviceName = review.getBooking().getService() != null ? 
                review.getBooking().getService().getTranslations().stream()
                    .map(translation -> translation.getName())
                    .findFirst()
                    .orElse("Unknown Service") : "Unknown Service";

            String serviceLocationAddress = review.getBooking().getServiceLocation() != null ?
                String.format("%s, %s, %s", 
                    review.getBooking().getServiceLocation().getStreet(),
                    review.getBooking().getServiceLocation().getCity(),
                    review.getBooking().getServiceLocation().getState()) : "No address provided";
            
            this.booking = new BookingInfo(
                review.getBooking().getId(),
                serviceName,
                review.getBooking().getScheduledDate(),
                review.getBooking().getTimeSchedule(),
                review.getBooking().getStatus(),
                review.getBooking().getTotalCost(),
                review.getBooking().getDescription(),
                serviceLocationAddress
            );
        }
    }
}
package com.home.service.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.models.Business;
import com.home.service.models.BusinessReview;
import com.home.service.models.User;
import com.home.service.models.enums.ReviewStatus;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.BusinessReviewRepository;
import com.home.service.repositories.UserRepository;
import com.home.service.services.FileStorageService;
import com.home.service.dto.ReviewRequest;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;

@Service
public class BusinessReviewService {

    private final BusinessReviewRepository businessReviewRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public BusinessReviewService(BusinessReviewRepository businessReviewRepository,
            BusinessRepository businessRepository,
            UserRepository userRepository,
            FileStorageService fileStorageService) {
        this.businessReviewRepository = businessReviewRepository;
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    public static class BusinessReviewDTO {
        public Long id;
        public Long businessId;
        public Long userId;
        public String name;
        public int rating;
        public String comment;
        public String response;
        public String responseDate;
        public ReviewStatus status;
        public List<String> images;
        public String date;

        public BusinessReviewDTO() {
        }

        public BusinessReviewDTO(BusinessReview review) {
            this.id = review.getId();
            this.businessId = review.getBusiness().getId();
            this.userId = review.getUser().getId();
            this.name = review.getUser().getName();
            this.rating = review.getRating();
            this.comment = review.getComment();
            this.response = review.getResponse();
            this.responseDate = review.getResponseDate() != null ? review.getResponseDate().toLocalDate().toString()
                    : null;
            this.status = review.getStatus();
            this.images = review.getImages();
            this.date = review.getCreatedAt().toLocalDate().toString();
        }
    }

    public BusinessReviewDTO createReview(@Valid ReviewRequest request, Long currentUserId) {
        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Business not found with ID: " + request.getBusinessId()));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + request.getUserId()));

        List<String> imageUrls = new ArrayList<>();
        if (request.getImages() != null) {
            for (MultipartFile image : request.getImages()) {
                String fileName = fileStorageService.storeFile(image);
                imageUrls.add(fileName);
            }
        }

        BusinessReview review = new BusinessReview();
        review.setBusiness(business);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setImages(imageUrls);
        review.setStatus(ReviewStatus.PENDING);

        BusinessReview savedReview = businessReviewRepository.save(review);
        return new BusinessReviewDTO(savedReview);
    }

    public BusinessReview getReviewById(Long id) {
        return businessReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with ID: " + id));
    }

    public BusinessReviewDTO updateReview(Long id, @Valid BusinessReviewDTO dto, Long currentUserId) {
        BusinessReview review = getReviewById(id);
        if (!review.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Only the reviewer can update this review");
        }

        Business business = businessRepository.findById(dto.businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + dto.businessId));

        review.setBusiness(business);
        review.setRating(dto.rating);
        review.setComment(dto.comment);
        review.setImages(dto.images);

        BusinessReview updatedReview = businessReviewRepository.save(review);
        return new BusinessReviewDTO(updatedReview);
    }

    public void deleteReview(Long id, Long currentUserId) {
        BusinessReview review = getReviewById(id);
        if (!review.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Only the reviewer can delete this review");
        }
        businessReviewRepository.delete(review);
    }

    public Page<BusinessReviewDTO> getReviewsByBusiness(Long businessId, int page, int size) {
        return getReviewsByBusiness(businessId, null, null, null, page, size);
    }

    public Page<BusinessReviewDTO> getReviewsByBusiness(
            Long businessId,
            String dateRange,
            Object rating,
            Object status,
            int page,
            int size) {

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + businessId));

        Specification<BusinessReview> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by business
            predicates.add(criteriaBuilder.equal(root.get("business"), business));

            // Filter by date range if provided
            if (dateRange != null && !dateRange.isEmpty() && !dateRange.equals("all")) {
                LocalDateTime startDateTime;
                LocalDateTime endDateTime = LocalDateTime.now();

                switch (dateRange) {
                    case "week":
                        startDateTime = LocalDateTime.now().minusWeeks(1);
                        break;
                    case "month":
                        startDateTime = LocalDateTime.now().minusMonths(1);
                        break;
                    case "year":
                        startDateTime = LocalDateTime.now().minusYears(1);
                        break;
                    default:
                        // If it's a custom date range in format "yyyy-MM-dd,yyyy-MM-dd"
                        if (dateRange.contains(",")) {
                            String[] dates = dateRange.split(",");
                            if (dates.length == 2) {
                                LocalDate startDate = LocalDate.parse(dates[0].trim());
                                LocalDate endDate = LocalDate.parse(dates[1].trim());

                                startDateTime = startDate.atStartOfDay();
                                endDateTime = endDate.plusDays(1).atStartOfDay();
                            } else {
                                startDateTime = LocalDateTime.now().minusYears(100); // Default to a very old date
                            }
                        } else {
                            startDateTime = LocalDateTime.now().minusYears(100); // Default to a very old date
                        }
                        break;
                }

                predicates.add(criteriaBuilder.between(root.get("createdAt"), startDateTime, endDateTime));
            }

            // Filter by rating if provided and not 'all'
            if (rating != null && !rating.equals("all")) {
                Integer ratingValue = null;
                if (rating instanceof String) {
                    try {
                        ratingValue = Integer.parseInt((String) rating);
                    } catch (NumberFormatException e) {
                        // Ignore parsing error, ratingValue remains null
                    }
                } else if (rating instanceof Integer) {
                    ratingValue = (Integer) rating;
                }

                if (ratingValue != null) {
                    predicates.add(criteriaBuilder.equal(root.get("rating"), ratingValue));
                }
            }

            // Filter by status if provided and not 'all'
            if (status != null && !status.equals("all")) {
                ReviewStatus statusValue = null;
                if (status instanceof String) {
                    try {
                        statusValue = ReviewStatus.valueOf((String) status);
                    } catch (IllegalArgumentException e) {
                        // Ignore parsing error, statusValue remains null
                    }
                } else if (status instanceof ReviewStatus) {
                    statusValue = (ReviewStatus) status;
                }

                if (statusValue != null) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), statusValue));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size);
        Page<BusinessReview> reviews = businessReviewRepository.findAll(spec, pageable);
        return reviews.map(BusinessReviewDTO::new);
    }

    public Page<BusinessReviewDTO> getReviewsByUser(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        Pageable pageable = PageRequest.of(page, size);
        Page<BusinessReview> reviews = businessReviewRepository.findByUser(user, pageable);
        return reviews.map(BusinessReviewDTO::new);
    }

    public BusinessReviewDTO respondToReview(Long reviewId, String response, Long currentUserId) {
        BusinessReview review = getReviewById(reviewId);

        // Check if the current user is the business owner
        // Business business = review.getBusiness();
        // if (!business.getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("Only the business owner can respond to this
        // review");
        // }

        review.setResponse(response);
        review.setResponseDate(LocalDateTime.now());
        review.setStatus(ReviewStatus.RESPONDED);

        BusinessReview updatedReview = businessReviewRepository.save(review);
        return new BusinessReviewDTO(updatedReview);
    }

    public BusinessReviewDTO updateReviewStatus(Long reviewId, ReviewStatus status, Long currentUserId) {
        BusinessReview review = getReviewById(reviewId);

        // Check if the current user is the business owner
        Business business = review.getBusiness();
        if (!business.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Only the business owner can update the review status");
        }

        // Only allow FLAGGED or ARCHIVED status updates
        if (status != ReviewStatus.FLAGGED && status != ReviewStatus.ARCHIVED) {
            throw new IllegalArgumentException("Status can only be updated to FLAGGED or ARCHIVED");
        }

        review.setStatus(status);

        BusinessReview updatedReview = businessReviewRepository.save(review);
        return new BusinessReviewDTO(updatedReview);
    }
}
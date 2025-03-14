package com.home.service.models;

import java.time.LocalDateTime;
import java.util.List;

import com.home.service.models.enums.ReviewStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "business_reviews")
@Getter
@Setter
public class BusinessReview extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Min(1)
    @Max(5)
    private int rating;

    private String comment;

    private String response;

    private LocalDateTime responseDate;

    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.PENDING;

    @ElementCollection
    private List<String> images;
}
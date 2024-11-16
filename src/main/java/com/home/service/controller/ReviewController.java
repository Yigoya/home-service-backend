package com.home.service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.home.service.Service.ReviewService;
import com.home.service.dto.ReviewRequest;

@RestController
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<String> submitReview(@RequestBody ReviewRequest reviewRequest) {
        reviewService.submitReview(reviewRequest);
        return ResponseEntity.ok("Review submitted successfully.");
    }
}

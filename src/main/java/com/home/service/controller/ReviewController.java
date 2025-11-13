package com.home.service.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.home.service.Service.ReviewService;
import com.home.service.dto.ReviewRequest;
import com.home.service.dto.TechnicianReviewDTO;

import java.util.List;

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

    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<Page<TechnicianReviewDTO>> getReviewsByTechnicianIdPageable(
            @PathVariable Long technicianId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Page<TechnicianReviewDTO> reviews = reviewService.getReviewsByTechnicianId(
            technicianId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(reviews);
    }
}

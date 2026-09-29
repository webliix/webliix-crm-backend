package com.webliix.review.controller;

import com.webliix.review.dto.CreateReviewRequest;
import com.webliix.review.dto.ReviewDTO;
import com.webliix.review.service.ReviewService;
import com.webliix.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/public/reviews", "/api/public/reviews"})
@RequiredArgsConstructor
public class PublicReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> getPublicApprovedReviews() {
        List<ReviewDTO> response = reviewService.getPublicApprovedReviews();
        return ResponseEntity.ok(ApiResponse.<List<ReviewDTO>>builder()
                .success(true)
                .message("Public approved reviews fetched")
                .data(response)
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDTO>> submitPublicReview(@Valid @RequestBody CreateReviewRequest request) {
        ReviewDTO response = reviewService.submitPublicReview(request);
        return ResponseEntity.ok(ApiResponse.<ReviewDTO>builder()
                .success(true)
                .message("Thank you for submitting your feedback! Your review is now live.")
                .data(response)
                .build());
    }
}

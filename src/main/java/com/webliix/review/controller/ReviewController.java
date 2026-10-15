package com.webliix.review.controller;

import com.webliix.review.dto.CreateReviewRequest;
import com.webliix.review.dto.ReviewDTO;
import com.webliix.review.service.ReviewService;
import com.webliix.security.annotation.RequirePermission;
import com.webliix.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@RequirePermission("REVIEWS_MANAGE")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReviewDTO>>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReviewDTO> response = reviewService.getAllReviewsAdmin(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<ReviewDTO>>builder()
                .success(true)
                .message("Reviews fetched successfully")
                .data(response)
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDTO>> createReview(@Valid @RequestBody CreateReviewRequest request) {
        ReviewDTO response = reviewService.submitPublicReview(request);
        return ResponseEntity.ok(ApiResponse.<ReviewDTO>builder()
                .success(true)
                .message("Review created successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{id}/toggle-approved")
    public ResponseEntity<ApiResponse<ReviewDTO>> toggleApproved(@PathVariable Long id) {
        ReviewDTO response = reviewService.toggleApproved(id);
        return ResponseEntity.ok(ApiResponse.<ReviewDTO>builder()
                .success(true)
                .message("Review approval status updated")
                .data(response)
                .build());
    }

    @PutMapping("/{id}/toggle-featured")
    public ResponseEntity<ApiResponse<ReviewDTO>> toggleFeatured(@PathVariable Long id) {
        ReviewDTO response = reviewService.toggleFeatured(id);
        return ResponseEntity.ok(ApiResponse.<ReviewDTO>builder()
                .success(true)
                .message("Review featured status updated")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Review deleted successfully")
                .build());
    }
}

package com.webliix.review.controller;

import com.webliix.review.dto.CreateReviewRequest;
import com.webliix.review.dto.ReviewDTO;
import com.webliix.review.service.ReviewService;
import com.webliix.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
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
    @PreAuthorize("hasAnyAuthority('REPORTS_VIEW', 'SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ReviewDTO>> createReview(@Valid @RequestBody CreateReviewRequest request) {
        ReviewDTO response = reviewService.submitPublicReview(request);
        return ResponseEntity.ok(ApiResponse.<ReviewDTO>builder()
                .success(true)
                .message("Review created successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{id}/toggle-approved")
    @PreAuthorize("hasAnyAuthority('REPORTS_VIEW', 'SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ReviewDTO>> toggleApproved(@PathVariable Long id) {
        ReviewDTO response = reviewService.toggleApproved(id);
        return ResponseEntity.ok(ApiResponse.<ReviewDTO>builder()
                .success(true)
                .message("Review approval status updated")
                .data(response)
                .build());
    }

    @PutMapping("/{id}/toggle-featured")
    @PreAuthorize("hasAnyAuthority('REPORTS_VIEW', 'SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ReviewDTO>> toggleFeatured(@PathVariable Long id) {
        ReviewDTO response = reviewService.toggleFeatured(id);
        return ResponseEntity.ok(ApiResponse.<ReviewDTO>builder()
                .success(true)
                .message("Review featured status updated")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('REPORTS_VIEW', 'SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Review deleted successfully")
                .build());
    }
}

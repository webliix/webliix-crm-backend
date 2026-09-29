package com.webliix.review.service;

import com.webliix.review.dto.CreateReviewRequest;
import com.webliix.review.dto.ReviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {

    List<ReviewDTO> getPublicApprovedReviews();

    ReviewDTO submitPublicReview(CreateReviewRequest request);

    Page<ReviewDTO> getAllReviewsAdmin(Pageable pageable);

    ReviewDTO toggleApproved(Long id);

    ReviewDTO toggleFeatured(Long id);

    void deleteReview(Long id);
}

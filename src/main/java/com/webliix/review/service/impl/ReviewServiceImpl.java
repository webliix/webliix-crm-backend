package com.webliix.review.service.impl;

import com.webliix.review.dto.CreateReviewRequest;
import com.webliix.review.dto.ReviewDTO;
import com.webliix.review.entity.Review;
import com.webliix.review.enums.ReviewPlatform;
import com.webliix.review.repository.ReviewRepository;
import com.webliix.review.service.ReviewService;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getPublicApprovedReviews() {
        return reviewRepository.findByApprovedTrueOrderByCreatedAtDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewDTO submitPublicReview(CreateReviewRequest request) {
        ReviewPlatform plat = request.getPlatform() != null ? request.getPlatform() : ReviewPlatform.WEBSITE;

        Review review = Review.builder()
                .authorName(request.getAuthorName().trim())
                .companyName(request.getCompanyName() != null ? request.getCompanyName().trim() : null)
                .email(request.getEmail() != null ? request.getEmail().trim() : null)
                .rating(request.getRating() != null ? request.getRating() : 5)
                .reviewText(request.getReviewText().trim())
                .platform(plat)
                .platformUrl(request.getPlatformUrl())
                .serviceUsed(request.getServiceUsed())
                .approved(true)
                .featured(false)
                .publishConsent(request.getPublishConsent() != null ? request.getPublishConsent() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Review saved = reviewRepository.save(review);
        eventPublisher.publishEvent(new com.webliix.review.event.ReviewSubmittedEvent(this, saved.getAuthorName(), saved.getCompanyName(), saved.getRating(), plat.name()));
        return toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getAllReviewsAdmin(Pageable pageable) {
        return reviewRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    @Transactional
    public ReviewDTO toggleApproved(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        review.setApproved(Boolean.FALSE.equals(review.getApproved()));
        review.setUpdatedAt(LocalDateTime.now());
        Review updated = reviewRepository.save(review);
        return toDTO(updated);
    }

    @Override
    @Transactional
    public ReviewDTO toggleFeatured(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        review.setFeatured(Boolean.FALSE.equals(review.getFeatured()));
        review.setUpdatedAt(LocalDateTime.now());
        Review updated = reviewRepository.save(review);
        return toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }

    private ReviewDTO toDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .authorName(review.getAuthorName())
                .companyName(review.getCompanyName())
                .email(review.getEmail())
                .rating(review.getRating())
                .reviewText(review.getReviewText())
                .platform(review.getPlatform())
                .platformUrl(review.getPlatformUrl())
                .serviceUsed(review.getServiceUsed())
                .approved(review.getApproved())
                .featured(review.getFeatured())
                .publishConsent(review.getPublishConsent())
                .createdAt(review.getCreatedAt())
                .build();
    }
}

package com.webliix.review.repository;

import com.webliix.review.entity.Review;
import com.webliix.review.enums.ReviewPlatform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByApprovedTrueOrderByCreatedAtDesc();

    List<Review> findByApprovedTrueAndFeaturedTrueOrderByCreatedAtDesc();

    Page<Review> findByPlatform(ReviewPlatform platform, Pageable pageable);

    Page<Review> findByAuthorNameContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(String name, String company, Pageable pageable);
}

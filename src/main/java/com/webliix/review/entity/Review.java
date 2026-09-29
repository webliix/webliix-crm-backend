package com.webliix.review.entity;

import com.webliix.review.enums.ReviewPlatform;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Column(name = "company_name")
    private String companyName;

    private String email;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "review_text", columnDefinition = "TEXT", nullable = false)
    private String reviewText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewPlatform platform;

    @Column(name = "platform_url")
    private String platformUrl;

    @Column(name = "service_used")
    private String serviceUsed;

    @Builder.Default
    private Boolean approved = true;

    @Builder.Default
    private Boolean featured = false;

    @Builder.Default
    @Column(name = "publish_consent")
    private Boolean publishConsent = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

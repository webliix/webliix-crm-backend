package com.webliix.review.dto;

import com.webliix.review.enums.ReviewPlatform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {

    private Long id;
    private String authorName;
    private String companyName;
    private String email;
    private Integer rating;
    private String reviewText;
    private ReviewPlatform platform;
    private String platformUrl;
    private String serviceUsed;
    private Boolean approved;
    private Boolean featured;
    private Boolean publishConsent;
    private LocalDateTime createdAt;
}

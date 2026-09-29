package com.webliix.review.dto;

import com.webliix.review.enums.ReviewPlatform;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {

    @NotBlank(message = "Author name is required")
    private String authorName;

    private String companyName;

    private String email;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;

    @NotBlank(message = "Review text is required")
    private String reviewText;

    private ReviewPlatform platform;

    private String platformUrl;

    private String serviceUsed;

    private Boolean publishConsent;
}

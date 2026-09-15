package com.webliix.blog.dto;

import com.webliix.blog.enums.BlogPostStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBlogPostRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String slug;

    private String summary;

    @NotBlank(message = "Content is required")
    private String content;

    private String coverImageUrl;

    private String coverImageAlt;

    private String coverImageCaption;

    private String authorName;

    private String category;

    private String tags;

    private BlogPostStatus status;

    private Boolean isFeatured;

    private Integer readingTimeMinutes;

    private String seoTitle;

    private String seoDescription;

    private String canonicalUrl;

    private String ogImageUrl;

    private LocalDateTime scheduledPublishAt;

    private Boolean enableAds;
    private String adSenseClientId;
    private String topAdSlotId;
    private String inlineAdSlotId;
    private String bottomAdSlotId;
    private String adFormat;
    private String coverImageAlignment;
    private String coverImageAspectRatio;
}

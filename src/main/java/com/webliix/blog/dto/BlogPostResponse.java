package com.webliix.blog.dto;

import com.webliix.blog.enums.BlogPostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPostResponse {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String coverImageUrl;
    private String coverImageAlt;
    private String coverImageCaption;
    private String authorName;
    private Long authorId;
    private String category;
    private String tags;
    private BlogPostStatus status;
    private Boolean isFeatured;
    private Long viewsCount;
    private Long likesCount;
    private Long commentsCount;
    private Integer readingTimeMinutes;
    private String seoTitle;
    private String seoDescription;
    private String canonicalUrl;
    private String ogImageUrl;
    private LocalDateTime scheduledPublishAt;
    private LocalDateTime publishedAt;
    private Boolean enableAds;
    private String adSenseClientId;
    private String topAdSlotId;
    private String inlineAdSlotId;
    private String bottomAdSlotId;
    private String adFormat;
    private String coverImageAlignment;
    private String coverImageAspectRatio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BlogCommentResponse> comments;
}

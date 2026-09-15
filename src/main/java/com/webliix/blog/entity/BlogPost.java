package com.webliix.blog.entity;

import com.webliix.blog.enums.BlogPostStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blog_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(length = 1000)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "cover_image_url", length = 1000)
    private String coverImageUrl;

    @Column(name = "cover_image_alt")
    private String coverImageAlt;

    @Column(name = "cover_image_caption", length = 500)
    private String coverImageCaption;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "author_id")
    private Long authorId;

    private String category;

    private String tags;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private BlogPostStatus status;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "views_count")
    @Builder.Default
    private Long viewsCount = 0L;

    @Column(name = "likes_count")
    @Builder.Default
    private Long likesCount = 0L;

    @Column(name = "comments_count")
    @Builder.Default
    private Long commentsCount = 0L;

    @Column(name = "reading_time_minutes")
    @Builder.Default
    private Integer readingTimeMinutes = 3;

    @Column(name = "seo_title")
    private String seoTitle;

    @Column(name = "seo_description", length = 500)
    private String seoDescription;

    @Column(name = "canonical_url", length = 500)
    private String canonicalUrl;

    @Column(name = "og_image_url", length = 1000)
    private String ogImageUrl;

    @Column(name = "scheduled_publish_at")
    private LocalDateTime scheduledPublishAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "enable_ads")
    @Builder.Default
    private Boolean enableAds = true;

    @Column(name = "adsense_client_id", length = 100)
    private String adSenseClientId;

    @Column(name = "top_ad_slot_id", length = 100)
    private String topAdSlotId;

    @Column(name = "inline_ad_slot_id", length = 100)
    private String inlineAdSlotId;

    @Column(name = "bottom_ad_slot_id", length = 100)
    private String bottomAdSlotId;

    @Column(name = "ad_format", length = 50)
    private String adFormat;

    @Column(name = "cover_image_alignment", length = 50)
    private String coverImageAlignment;

    @Column(name = "cover_image_aspect_ratio", length = 50)
    private String coverImageAspectRatio;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

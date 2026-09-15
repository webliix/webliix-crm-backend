package com.webliix.blog.entity;

import com.webliix.blog.enums.BlogResourceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blog_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false)
    private String publicId;

    @Column(name = "secure_url", nullable = false, length = 1000)
    private String secureUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 50)
    @Builder.Default
    private BlogResourceType resourceType = BlogResourceType.IMAGE;

    private String format;

    private Integer width;

    private Integer height;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "alt_text")
    private String altText;

    @Column(length = 500)
    private String caption;

    private String folder;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

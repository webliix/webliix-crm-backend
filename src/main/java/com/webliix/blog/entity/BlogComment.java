package com.webliix.blog.entity;

import com.webliix.blog.enums.BlogCommentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blog_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Column(name = "author_email")
    private String authorEmail;

    @Column(name = "author_website")
    private String authorWebsite;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @Builder.Default
    private BlogCommentStatus status = BlogCommentStatus.APPROVED;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

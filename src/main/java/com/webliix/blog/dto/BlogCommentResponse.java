package com.webliix.blog.dto;

import com.webliix.blog.enums.BlogCommentStatus;
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
public class BlogCommentResponse {

    private Long id;
    private Long postId;
    private Long parentId;
    private String authorName;
    private String authorEmail;
    private String authorWebsite;
    private String content;
    private BlogCommentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BlogCommentResponse> replies;
}

package com.webliix.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogLikeResponse {
    private Long postId;
    private String slug;
    private long likesCount;
    private boolean recorded;
    private String message;
}

package com.webliix.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogStatisticsResponse {

    private long totalPosts;
    private long publishedPosts;
    private long draftPosts;
    private long totalViews;
    private long totalLikes;
    private long totalComments;
}

package com.webliix.blog.dto;

import com.webliix.blog.enums.BlogResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogMediaResponse {

    private Long id;
    private String publicId;
    private String secureUrl;
    private BlogResourceType resourceType;
    private String format;
    private Integer width;
    private Integer height;
    private Long fileSize;
    private String altText;
    private String caption;
    private String folder;
    private Long postId;
    private LocalDateTime createdAt;
}

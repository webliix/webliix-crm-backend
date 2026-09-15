package com.webliix.blog.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBlogCommentRequest {

    private Long parentId;

    @JsonAlias({"name", "author", "author_name", "user", "commenterName"})
    private String authorName;

    @JsonAlias({"email", "author_email", "userEmail"})
    private String authorEmail;

    @JsonAlias({"website", "author_website", "url", "userWebsite"})
    private String authorWebsite;

    @NotBlank(message = "Comment text is required")
    @JsonAlias({"text", "comment", "body", "message", "comment_text", "commentText"})
    private String content;
}

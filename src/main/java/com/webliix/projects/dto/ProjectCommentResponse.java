package com.webliix.projects.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectCommentResponse {

    private Long id;
    private Long projectId;
    private Long taskId;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private String message;
    private LocalDateTime createdAt;

    public String getComment() {
        return message;
    }

    public void setComment(String comment) {
        if (this.message == null || this.message.isBlank()) {
            this.message = comment;
        }
    }
}


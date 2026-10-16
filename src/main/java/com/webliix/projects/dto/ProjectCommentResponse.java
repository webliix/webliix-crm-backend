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
    private String comment;
    private LocalDateTime createdAt;

    public String getComment() {
        return (comment != null && !comment.isBlank()) ? comment : message;
    }

    public void setComment(String comment) {
        this.comment = comment;
        if (this.message == null || this.message.isBlank()) {
            this.message = comment;
        }
    }

    public String getMessage() {
        return (message != null && !message.isBlank()) ? message : comment;
    }

    public void setMessage(String message) {
        this.message = message;
        if (this.comment == null || this.comment.isBlank()) {
            this.comment = message;
        }
    }
}


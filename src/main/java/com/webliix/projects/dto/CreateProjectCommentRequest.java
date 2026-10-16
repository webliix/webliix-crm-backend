package com.webliix.projects.dto;

import lombok.Data;

@Data
public class CreateProjectCommentRequest {

    private Long authorId;
    private String authorName;
    private String authorRole;
    @com.fasterxml.jackson.annotation.JsonAlias({"comment", "message"})
    private String message;

    public String getComment() {
        return message;
    }

    public void setComment(String comment) {
        if (this.message == null || this.message.isBlank()) {
            this.message = comment;
        }
    }
}


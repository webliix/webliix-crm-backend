package com.webliix.projects.dto;

import lombok.Data;

@Data
public class CreateProjectCommentRequest {

    private Long authorId;
    private String message;
}


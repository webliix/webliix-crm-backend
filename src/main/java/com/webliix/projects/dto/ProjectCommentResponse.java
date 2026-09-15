package com.webliix.projects.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectCommentResponse {

    private Long id;
    private Long projectId;
    private Long taskId;
    private Long authorId;
    private String message;
    private LocalDateTime createdAt;
}


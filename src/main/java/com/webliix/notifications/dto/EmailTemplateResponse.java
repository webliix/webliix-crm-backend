package com.webliix.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateResponse {
    private Long id;
    private String templateKey;
    private String subject;
    private String htmlContent;
    private String plainTextContent;
    private Boolean isActive;
    private String description;
    private String sampleVariables;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

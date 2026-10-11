package com.webliix.notifications.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmailTemplateRequest {
    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "HTML content is required")
    private String htmlContent;

    private String plainTextContent;
    private Boolean isActive;
    private String description;
}

package com.webliix.notifications.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestSendEmailTemplateRequest {
    @NotBlank(message = "Recipient email is required")
    @Email(message = "Must be a valid email address")
    private String recipientEmail;

    private String subject;
    private String htmlContent;
    private Map<String, String> variables;
}

package com.webliix.crm.lead.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendLeadEmailRequest {

    private String recipientEmail;

    private String recipientName;

    @NotBlank(message = "Subject cannot be empty")
    private String subject;

    @NotBlank(message = "Message body cannot be empty")
    private String message;

    private String ctaText;

    private String ctaUrl;
}

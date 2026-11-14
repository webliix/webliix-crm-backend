package com.webliix.crm.customer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastCustomerEmailRequest {

    @NotBlank(message = "Email subject is required")
    private String subject;

    @NotBlank(message = "Email message content is required")
    private String message;

    private String templateKey;
    private String ctaText;
    private String ctaUrl;
}

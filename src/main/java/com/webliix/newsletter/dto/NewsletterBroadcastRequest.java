package com.webliix.newsletter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterBroadcastRequest {

    @NotBlank(message = "Email subject is required")
    private String subject;

    @NotBlank(message = "Broadcast content/body is required")
    private String content;
}

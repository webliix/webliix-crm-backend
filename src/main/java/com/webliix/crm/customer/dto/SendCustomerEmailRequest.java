package com.webliix.crm.customer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendCustomerEmailRequest {

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Message body is required")
    private String message;

    private String recipientEmail;
}

package com.webliix.crm.lead.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicLeadRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String companyName;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    private String phone;

    private String requirements;

    private String serviceRequested;

    private BigDecimal estimatedBudget;

    // Anti-spam honeypot trap field (should remain empty for real users)
    private String honeypot;

    // Attribution Data
    private String source;
    private String page;
    private String referrer;
    private String utmSource;
    private String utmMedium;
    private String utmCampaign;
    private String utmTerm;
    private String utmContent;
}

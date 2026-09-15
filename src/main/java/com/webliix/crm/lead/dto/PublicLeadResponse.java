package com.webliix.crm.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicLeadResponse {

    private Long leadId;
    private String contactPerson;
    private String companyName;
    private String email;
    private String phone;
    private String status;
    private String whatsappConnectUrl;
    private String message;
    private LocalDateTime createdAt;
}

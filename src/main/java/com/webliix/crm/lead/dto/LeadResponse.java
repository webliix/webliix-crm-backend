package com.webliix.crm.lead.dto;

import com.webliix.crm.lead.enums.LeadSource;
import com.webliix.crm.lead.enums.LeadStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeadResponse {

    private Long id;
    private String companyName;
    private String contactPerson;
    private String email;
    private String phone;
    private String website;
    private String address;
    private String city;
    private String state;
    private String country;
    private String requirements;
    private BigDecimal estimatedValue;
    private LeadStatus status;
    private LeadSource source;
    private LocalDate nextFollowUpDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

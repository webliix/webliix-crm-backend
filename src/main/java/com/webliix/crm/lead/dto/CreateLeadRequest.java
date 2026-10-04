package com.webliix.crm.lead.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateLeadRequest {

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

    private String source;

    private String status;

    private LocalDate nextFollowUpDate;

    private String notes;
}

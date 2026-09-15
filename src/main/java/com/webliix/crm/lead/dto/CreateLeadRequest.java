package com.webliix.crm.lead.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateLeadRequest {

    private String companyName;

    private String contactPerson;

    private String email;

    private String phone;

    private String requirements;

    private BigDecimal estimatedValue;

    private String source;
}

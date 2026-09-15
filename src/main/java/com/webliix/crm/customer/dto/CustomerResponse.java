package com.webliix.crm.customer.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CustomerResponse {

    private Long id;
    private String companyName;
    private String customerCode;
    private String contactPerson;
    private String email;
    private String phone;
    private String website;
    private String gstNumber;
    private String address;
    private String city;
    private String state;
    private String country;
    private BigDecimal lifetimeValue;
    private LocalDate customerSince;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.webliix.crm.customer.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateCustomerRequest {

    private String companyName;

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
}

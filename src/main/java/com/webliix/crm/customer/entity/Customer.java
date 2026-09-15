package com.webliix.crm.customer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "customer_code", unique = true)
    private String customerCode;

    private String contactPerson;

    private String email;

    private String phone;

    private String website;

    @Column(name = "gst_number")
    private String gstNumber;

    private String address;

    private String city;

    private String state;

    private String country;

    @Column(name = "lifetime_value", precision = 19, scale = 2)
    private BigDecimal lifetimeValue;

    @Column(name = "customer_since")
    private LocalDate customerSince;

    private Boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

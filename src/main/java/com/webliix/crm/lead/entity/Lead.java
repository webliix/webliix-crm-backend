package com.webliix.crm.lead.entity;

import com.webliix.crm.lead.enums.LeadSource;
import com.webliix.crm.lead.enums.LeadStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "contact_person")
    private String contactPerson;

    private String email;

    private String phone;

    private String website;

    private String address;

    private String city;

    private String state;

    private String country;

    private String requirements;

    @Column(name = "estimated_value", precision = 19, scale = 2)
    private BigDecimal estimatedValue;

    @Enumerated(EnumType.STRING)
    private LeadStatus status;

    @Enumerated(EnumType.STRING)
    private LeadSource source;

    @Column(name = "next_follow_up_date")
    private LocalDate nextFollowUpDate;

    private String notes;

    private Boolean converted;

    @Column(name = "converted_at")
    private LocalDateTime convertedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

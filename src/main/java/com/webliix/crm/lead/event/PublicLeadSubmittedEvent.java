package com.webliix.crm.lead.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class PublicLeadSubmittedEvent extends ApplicationEvent {

    private final Long leadId;
    private final String contactPerson;
    private final String companyName;
    private final String email;
    private final String phone;
    private final String requirements;
    private final String serviceRequested;
    private final BigDecimal estimatedBudget;
    private final String source;
    private final String page;
    private final LocalDateTime createdAt;

    public PublicLeadSubmittedEvent(
            Object sourceObject,
            Long leadId,
            String contactPerson,
            String companyName,
            String email,
            String phone,
            String requirements,
            String serviceRequested,
            BigDecimal estimatedBudget,
            String source,
            String page,
            LocalDateTime createdAt
    ) {
        super(sourceObject);
        this.leadId = leadId;
        this.contactPerson = contactPerson;
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
        this.requirements = requirements;
        this.serviceRequested = serviceRequested;
        this.estimatedBudget = estimatedBudget;
        this.source = source;
        this.page = page;
        this.createdAt = createdAt;
    }
}

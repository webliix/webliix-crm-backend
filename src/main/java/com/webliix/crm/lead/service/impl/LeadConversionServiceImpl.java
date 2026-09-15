package com.webliix.crm.lead.service.impl;

import com.webliix.crm.customer.entity.Customer;
import com.webliix.crm.customer.repository.CustomerRepository;
import com.webliix.crm.customer.service.CustomerCodeGenerator;
import com.webliix.crm.lead.entity.Lead;
import com.webliix.crm.lead.enums.LeadStatus;
import com.webliix.crm.lead.repository.LeadRepository;
import com.webliix.crm.lead.service.LeadConversionService;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LeadConversionServiceImpl implements LeadConversionService {

    private final LeadRepository leadRepository;
    private final CustomerRepository customerRepository;
    private final CustomerCodeGenerator customerCodeGenerator;

    @Override
    public Customer convertLead(Long leadId) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + leadId));

        if (lead.getStatus() != LeadStatus.WON) {
            throw new IllegalStateException("Lead must be WON before conversion");
        }

        Customer customer = Customer.builder()
                .companyName(lead.getCompanyName())
                .contactPerson(lead.getContactPerson())
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .website(lead.getWebsite())
                .address(lead.getAddress())
                .city(lead.getCity())
                .state(lead.getState())
                .country(lead.getCountry())
                .lifetimeValue(lead.getEstimatedValue())
                .customerSince(LocalDate.now())
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .customerCode(customerCodeGenerator.generateNextCustomerCode())
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        lead.setConverted(true);
        lead.setConvertedAt(LocalDateTime.now());
        leadRepository.save(lead);

        return savedCustomer;
    }
}

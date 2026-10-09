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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadConversionServiceImpl implements LeadConversionService {

    private final LeadRepository leadRepository;
    private final CustomerRepository customerRepository;
    private final CustomerCodeGenerator customerCodeGenerator;
    private final com.webliix.crm.customer.service.CustomerService customerService;

    @Override
    @Transactional
    public Customer convertLead(Long leadId) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + leadId));

        if (Boolean.TRUE.equals(lead.getConverted())) {
            throw new IllegalArgumentException("Lead #" + leadId + " has already been converted into a customer.");
        }

        String companyName = lead.getCompanyName() != null && !lead.getCompanyName().isBlank()
                ? lead.getCompanyName()
                : (lead.getContactPerson() != null && !lead.getContactPerson().isBlank() ? lead.getContactPerson() : "Client #" + leadId);

        String contactPerson = lead.getContactPerson() != null && !lead.getContactPerson().isBlank()
                ? lead.getContactPerson()
                : companyName;

        Customer customer = Customer.builder()
                .companyName(companyName)
                .contactPerson(contactPerson)
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
        customerService.provisionClientPortalAccount(savedCustomer);

        // Mark lead as WON and CONVERTED
        lead.setStatus(LeadStatus.WON);
        lead.setConverted(true);
        lead.setConvertedAt(LocalDateTime.now());
        lead.setUpdatedAt(LocalDateTime.now());
        leadRepository.save(lead);

        log.info("Lead #{} converted successfully into Customer #{} ({})", leadId, savedCustomer.getId(), savedCustomer.getCustomerCode());
        return savedCustomer;
    }
}

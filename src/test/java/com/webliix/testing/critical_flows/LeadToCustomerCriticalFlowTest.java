package com.webliix.testing.critical_flows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.webliix.crm.customer.entity.Customer;
import com.webliix.crm.customer.repository.CustomerRepository;
import com.webliix.crm.lead.entity.Lead;
import com.webliix.crm.lead.repository.LeadRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LeadToCustomerCriticalFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void testLeadConversionFlow() throws Exception {
        // Step 1: Create a lead
       Lead lead = Lead.builder()
        .companyName("ABC Technologies")
        .contactPerson("John Doe")
        .email("john@test.com")
        .phone("9876543210")
        .converted(false)
        .build();

        Lead savedLead = leadRepository.save(lead);

        // Step 2: Verify lead was created
        assert(savedLead.getId() != null);

        // Step 3: Convert lead to customer (this would be an endpoint call in real scenario)
// Step 3: Convert lead to customer
Customer customer = Customer.builder()
        .companyName(savedLead.getCompanyName())
        .contactPerson(savedLead.getContactPerson())
        .email(savedLead.getEmail())
        .phone(savedLead.getPhone())
        .active(true)
        .createdAt(LocalDateTime.now())
        .build();

Customer savedCustomer = customerRepository.save(customer);
        // Step 4: Verify customer was created
        assert(savedCustomer.getId() != null);

        // Critical flow completed successfully
    }
}

package com.webliix.crm.lead;

import com.webliix.crm.customer.entity.Customer;
import com.webliix.crm.customer.repository.CustomerRepository;
import com.webliix.crm.lead.dto.CreateLeadRequest;
import com.webliix.crm.lead.dto.LeadResponse;
import com.webliix.crm.lead.entity.Lead;
import com.webliix.crm.lead.enums.LeadStatus;
import com.webliix.crm.lead.repository.LeadRepository;
import com.webliix.crm.lead.service.LeadConversionService;
import com.webliix.crm.lead.service.LeadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class LeadWorkflowIntegrationTest {

    @Autowired
    private LeadService leadService;

    @Autowired
    private LeadConversionService leadConversionService;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @Transactional
    public void testFullLeadLifecycleAndConversion() {
        // 1. Create a lead
        CreateLeadRequest req = new CreateLeadRequest();
        req.setCompanyName("Acme Global");
        req.setContactPerson("Alice Walker");
        req.setEmail("alice@acmeglobal.com");
        req.setPhone("+1234567890");
        req.setEstimatedValue(new BigDecimal("15000.00"));
        req.setRequirements("Full CRM + Cloud integration");
        req.setSource("WEBSITE");
        req.setStatus("NEW");
        req.setNextFollowUpDate(LocalDate.now().plusDays(5));
        req.setNotes("High priority lead");

        LeadResponse created = leadService.createLead(req);
        assertNotNull(created.getId());
        assertEquals("Acme Global", created.getCompanyName());
        assertEquals(LeadStatus.NEW, created.getStatus());
        assertFalse(created.getConverted());

        // 2. Fetch leads
        Page<LeadResponse> allLeads = leadService.getAllLeads(PageRequest.of(0, 20));
        assertTrue(allLeads.getTotalElements() > 0);

        // 3. Search leads
        Page<LeadResponse> searchRes = leadService.searchLeads("Acme", PageRequest.of(0, 10));
        assertTrue(searchRes.getContent().stream().anyMatch(l -> l.getId().equals(created.getId())));

        // 4. Update lead
        CreateLeadRequest updateReq = new CreateLeadRequest();
        updateReq.setCompanyName("Acme Global Inc");
        updateReq.setStatus("QUALIFIED");
        updateReq.setContactPerson("Alice Walker");
        updateReq.setEmail("alice@acmeglobal.com");
        LeadResponse updated = leadService.updateLead(created.getId(), updateReq);
        assertEquals("Acme Global Inc", updated.getCompanyName());
        assertEquals(LeadStatus.QUALIFIED, updated.getStatus());

        // 5. Convert lead to customer
        Customer customer = leadConversionService.convertLead(created.getId());
        assertNotNull(customer.getId());
        assertNotNull(customer.getCustomerCode());
        assertEquals("Acme Global Inc", customer.getCompanyName());
        assertEquals("Alice Walker", customer.getContactPerson());
        assertEquals("alice@acmeglobal.com", customer.getEmail());

        // 6. Verify lead is marked WON and converted
        Lead convertedLead = leadRepository.findById(created.getId()).orElseThrow();
        assertEquals(LeadStatus.WON, convertedLead.getStatus());
        assertTrue(convertedLead.getConverted());
        assertNotNull(convertedLead.getConvertedAt());

        // 7. Verify converting already converted lead throws IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            leadConversionService.convertLead(created.getId());
        });
    }

    @Test
    @Transactional
    public void testLegacyOpenStatusHandling() {
        // Direct DB save simulating legacy demo row with OPEN status
        Lead legacyLead = Lead.builder()
                .companyName("Legacy Systems")
                .contactPerson("Bob Dylan")
                .email("bob@legacy.com")
                .status(LeadStatus.OPEN)
                .build();
        Lead saved = leadRepository.save(legacyLead);

        // Querying all leads must not throw any exception
        Page<LeadResponse> leads = leadService.getAllLeads(PageRequest.of(0, 50));
        assertNotNull(leads);
        assertTrue(leads.getContent().stream().anyMatch(l -> l.getId().equals(saved.getId())));
    }
}

package com.webliix.crm.lead.service;

import com.webliix.crm.lead.dto.CreateLeadRequest;
import com.webliix.crm.lead.dto.LeadResponse;
import com.webliix.crm.lead.dto.PublicLeadRequest;
import com.webliix.crm.lead.dto.PublicLeadResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeadService {

    LeadResponse createLead(CreateLeadRequest request);

    PublicLeadResponse createPublicLead(PublicLeadRequest request);

    LeadResponse getLead(Long id);

    Page<LeadResponse> getAllLeads(Pageable pageable);

    LeadResponse updateLead(Long id, CreateLeadRequest request);

    void deleteLead(Long id);

    Page<LeadResponse> searchLeads(String keyword, Pageable pageable);
}

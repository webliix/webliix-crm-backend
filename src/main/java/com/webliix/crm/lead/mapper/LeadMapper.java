package com.webliix.crm.lead.mapper;

import com.webliix.crm.lead.dto.CreateLeadRequest;
import com.webliix.crm.lead.dto.LeadResponse;
import com.webliix.crm.lead.entity.Lead;
import com.webliix.crm.lead.enums.LeadSource;
import com.webliix.crm.lead.enums.LeadStatus;

import java.time.LocalDateTime;

public class LeadMapper {

    public static Lead toEntity(CreateLeadRequest req) {
        Lead lead = new Lead();
        lead.setCompanyName(req.getCompanyName());
        lead.setContactPerson(req.getContactPerson());
        lead.setEmail(req.getEmail());
        lead.setPhone(req.getPhone());
        lead.setRequirements(req.getRequirements());
        lead.setEstimatedValue(req.getEstimatedValue());
        if (req.getSource() != null) {
            try {
                lead.setSource(LeadSource.valueOf(req.getSource()));
            } catch (IllegalArgumentException ex) {
                lead.setSource(LeadSource.OTHER);
            }
        }
        lead.setCreatedAt(LocalDateTime.now());
        lead.setUpdatedAt(LocalDateTime.now());
        lead.setStatus(LeadStatus.NEW);
        return lead;
    }

    public static LeadResponse toResponse(Lead lead) {
        LeadResponse res = new LeadResponse();
        res.setId(lead.getId());
        res.setCompanyName(lead.getCompanyName());
        res.setContactPerson(lead.getContactPerson());
        res.setEmail(lead.getEmail());
        res.setPhone(lead.getPhone());
        res.setWebsite(lead.getWebsite());
        res.setAddress(lead.getAddress());
        res.setCity(lead.getCity());
        res.setState(lead.getState());
        res.setCountry(lead.getCountry());
        res.setRequirements(lead.getRequirements());
        res.setEstimatedValue(lead.getEstimatedValue());
        res.setStatus(lead.getStatus());
        res.setSource(lead.getSource());
        res.setNextFollowUpDate(lead.getNextFollowUpDate());
        res.setNotes(lead.getNotes());
        res.setCreatedAt(lead.getCreatedAt());
        res.setUpdatedAt(lead.getUpdatedAt());
        return res;
    }

    public static void updateEntity(Lead lead, CreateLeadRequest req) {
        if (req.getCompanyName() != null) lead.setCompanyName(req.getCompanyName());
        if (req.getContactPerson() != null) lead.setContactPerson(req.getContactPerson());
        if (req.getEmail() != null) lead.setEmail(req.getEmail());
        if (req.getPhone() != null) lead.setPhone(req.getPhone());
        if (req.getRequirements() != null) lead.setRequirements(req.getRequirements());
        if (req.getEstimatedValue() != null) lead.setEstimatedValue(req.getEstimatedValue());
        if (req.getSource() != null) {
            try {
                lead.setSource(LeadSource.valueOf(req.getSource()));
            } catch (IllegalArgumentException ex) {
                lead.setSource(LeadSource.OTHER);
            }
        }
        lead.setUpdatedAt(LocalDateTime.now());
    }
}

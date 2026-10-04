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
        lead.setWebsite(req.getWebsite());
        lead.setAddress(req.getAddress());
        lead.setCity(req.getCity());
        lead.setState(req.getState());
        lead.setCountry(req.getCountry());
        lead.setRequirements(req.getRequirements());
        lead.setEstimatedValue(req.getEstimatedValue());
        lead.setNextFollowUpDate(req.getNextFollowUpDate());
        lead.setNotes(req.getNotes());
        lead.setConverted(false);

        if (req.getSource() != null && !req.getSource().isBlank()) {
            try {
                lead.setSource(LeadSource.valueOf(req.getSource().trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                lead.setSource(LeadSource.OTHER);
            }
        } else {
            lead.setSource(LeadSource.WEBSITE);
        }

        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            try {
                String normalizedStatus = req.getStatus().trim().toUpperCase();
                if ("OPEN".equals(normalizedStatus)) {
                    lead.setStatus(LeadStatus.NEW);
                } else {
                    lead.setStatus(LeadStatus.valueOf(normalizedStatus));
                }
            } catch (IllegalArgumentException ex) {
                lead.setStatus(LeadStatus.NEW);
            }
        } else {
            lead.setStatus(LeadStatus.NEW);
        }

        lead.setCreatedAt(LocalDateTime.now());
        lead.setUpdatedAt(LocalDateTime.now());
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
        res.setStatus(lead.getStatus() != null ? lead.getStatus() : LeadStatus.NEW);
        res.setSource(lead.getSource() != null ? lead.getSource() : LeadSource.WEBSITE);
        res.setNextFollowUpDate(lead.getNextFollowUpDate());
        res.setNotes(lead.getNotes());
        res.setConverted(Boolean.TRUE.equals(lead.getConverted()));
        res.setConvertedAt(lead.getConvertedAt());
        res.setCreatedAt(lead.getCreatedAt());
        res.setUpdatedAt(lead.getUpdatedAt());
        return res;
    }

    public static void updateEntity(Lead lead, CreateLeadRequest req) {
        if (req.getCompanyName() != null) lead.setCompanyName(req.getCompanyName());
        if (req.getContactPerson() != null) lead.setContactPerson(req.getContactPerson());
        if (req.getEmail() != null) lead.setEmail(req.getEmail());
        if (req.getPhone() != null) lead.setPhone(req.getPhone());
        if (req.getWebsite() != null) lead.setWebsite(req.getWebsite());
        if (req.getAddress() != null) lead.setAddress(req.getAddress());
        if (req.getCity() != null) lead.setCity(req.getCity());
        if (req.getState() != null) lead.setState(req.getState());
        if (req.getCountry() != null) lead.setCountry(req.getCountry());
        if (req.getRequirements() != null) lead.setRequirements(req.getRequirements());
        if (req.getEstimatedValue() != null) lead.setEstimatedValue(req.getEstimatedValue());
        if (req.getNextFollowUpDate() != null) lead.setNextFollowUpDate(req.getNextFollowUpDate());
        if (req.getNotes() != null) lead.setNotes(req.getNotes());

        if (req.getSource() != null && !req.getSource().isBlank()) {
            try {
                lead.setSource(LeadSource.valueOf(req.getSource().trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                lead.setSource(LeadSource.OTHER);
            }
        }

        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            try {
                String normalizedStatus = req.getStatus().trim().toUpperCase();
                if ("OPEN".equals(normalizedStatus)) {
                    lead.setStatus(LeadStatus.NEW);
                } else {
                    lead.setStatus(LeadStatus.valueOf(normalizedStatus));
                }
            } catch (IllegalArgumentException ex) {
                // Keep existing status if invalid string passed
            }
        }

        lead.setUpdatedAt(LocalDateTime.now());
    }
}

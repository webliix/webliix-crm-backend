package com.webliix.crm.lead.service.impl;

import com.webliix.crm.lead.dto.CreateLeadRequest;
import com.webliix.crm.lead.dto.LeadResponse;
import com.webliix.crm.lead.dto.PublicLeadRequest;
import com.webliix.crm.lead.dto.PublicLeadResponse;
import com.webliix.crm.lead.entity.Lead;
import com.webliix.crm.lead.enums.LeadSource;
import com.webliix.crm.lead.enums.LeadStatus;
import com.webliix.crm.lead.mapper.LeadMapper;
import com.webliix.crm.lead.repository.LeadRepository;
import com.webliix.crm.lead.service.LeadService;
import com.webliix.notifications.event.LeadCreatedEvent;
import com.webliix.notifications.service.EmailService;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final EmailService emailService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public LeadResponse createLead(CreateLeadRequest request) {
        Lead lead = LeadMapper.toEntity(request);
        Lead saved = leadRepository.save(lead);
        eventPublisher.publishEvent(new LeadCreatedEvent(this, saved.getId(), saved.getContactPerson(), saved.getEmail()));
        return LeadMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PublicLeadResponse createPublicLead(PublicLeadRequest request) {
        String companyName = request.getCompanyName() != null && !request.getCompanyName().isBlank()
                ? request.getCompanyName().trim()
                : request.getName().trim();

        String requirements = request.getRequirements() != null ? request.getRequirements().trim() : "";
        if (request.getServiceRequested() != null && !request.getServiceRequested().isBlank()) {
            requirements = "[Service: " + request.getServiceRequested().trim() + "] " + requirements;
        }

        Lead lead = Lead.builder()
                .companyName(companyName)
                .contactPerson(request.getName().trim())
                .email(request.getEmail().trim())
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .requirements(requirements)
                .estimatedValue(request.getEstimatedBudget())
                .source(LeadSource.WEBSITE)
                .status(LeadStatus.NEW)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Lead saved = leadRepository.save(lead);

        // 1. Automated welcome and requirement inquiry email
        try {
            String emailSubject = "Thank you for contacting Webliix – Let's discuss your requirements";
            String emailBody = "Hello " + request.getName() + ",\n\n"
                    + "Thank you for reaching out to Webliix! We have successfully received your project inquiry:\n\n"
                    + "Requirements Summary:\n\"" + requirements + "\"\n\n"
                    + "Our solutions team is reviewing your project details. We would love to understand any specific technical requirements or timelines you have.\n\n"
                    + "Feel free to reply directly to this email with any additional documents or specifications.\n\n"
                    + "Best regards,\nWebliix Team\nhttps://webliix.in";
            emailService.sendEmail(request.getEmail(), emailSubject, emailBody);
        } catch (Exception ex) {
            log.warn("Could not send automated lead email: {}", ex.getMessage());
        }

        // 2. Automated WhatsApp connect URL generation
        String cleanPhone = request.getPhone() != null ? request.getPhone().replaceAll("[^0-9]", "") : "";
        String whatsappUrl = null;
        if (!cleanPhone.isEmpty()) {
            String greetingText = URLEncoder.encode(
                    "Hello " + request.getName() + ", thank you for reaching out to Webliix! We received your project inquiry: \"" + requirements + "\". We would love to discuss your requirements.",
                    StandardCharsets.UTF_8
            );
            whatsappUrl = "https://wa.me/" + cleanPhone + "?text=" + greetingText;
        }

        // 3. Publish event for CRM notification listeners
        eventPublisher.publishEvent(new LeadCreatedEvent(this, saved.getId(), saved.getContactPerson(), saved.getEmail()));

        return PublicLeadResponse.builder()
                .leadId(saved.getId())
                .contactPerson(saved.getContactPerson())
                .companyName(saved.getCompanyName())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .status(saved.getStatus().name())
                .whatsappConnectUrl(whatsappUrl)
                .message("Lead registered successfully. Automated inquiry dispatched.")
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    public LeadResponse getLead(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));
        return LeadMapper.toResponse(lead);
    }

    @Override
    public Page<LeadResponse> getAllLeads(Pageable pageable) {
        return leadRepository.findAll(pageable).map(LeadMapper::toResponse);
    }

    @Override
    public LeadResponse updateLead(Long id, CreateLeadRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));
        LeadMapper.updateEntity(lead, request);
        Lead updated = leadRepository.save(lead);
        return LeadMapper.toResponse(updated);
    }

    @Override
    public void deleteLead(Long id) {
        if (!leadRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lead not found with id: " + id);
        }
        leadRepository.deleteById(id);
    }

    @Override
    public Page<LeadResponse> searchLeads(String keyword, Pageable pageable) {
        return leadRepository.findByCompanyNameContainingIgnoreCase(keyword, pageable)
                .map(LeadMapper::toResponse);
    }
}

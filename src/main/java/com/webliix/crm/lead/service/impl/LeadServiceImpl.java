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
        String cleanName = sanitize(request.getName());
        String cleanEmail = sanitize(request.getEmail()).toLowerCase();
        String cleanPhone = request.getPhone() != null ? sanitize(request.getPhone()) : null;
        String companyName = request.getCompanyName() != null && !request.getCompanyName().isBlank()
                ? sanitize(request.getCompanyName())
                : cleanName;

        StringBuilder reqBuilder = new StringBuilder();
        if (request.getServiceRequested() != null && !request.getServiceRequested().isBlank()) {
            reqBuilder.append("[Service: ").append(sanitize(request.getServiceRequested())).append("] ");
        }
        if (request.getSource() != null && !request.getSource().isBlank()) {
            reqBuilder.append("[Source: ").append(sanitize(request.getSource())).append("] ");
        }
        if (request.getPage() != null && !request.getPage().isBlank()) {
            reqBuilder.append("[Page: ").append(sanitize(request.getPage())).append("] ");
        }
        if (request.getUtmSource() != null && !request.getUtmSource().isBlank()) {
            reqBuilder.append("[UTM Source: ").append(sanitize(request.getUtmSource())).append("] ");
        }
        if (request.getUtmCampaign() != null && !request.getUtmCampaign().isBlank()) {
            reqBuilder.append("[UTM Campaign: ").append(sanitize(request.getUtmCampaign())).append("] ");
        }
        if (request.getRequirements() != null && !request.getRequirements().isBlank()) {
            reqBuilder.append(sanitize(request.getRequirements()));
        }

        String fullRequirements = reqBuilder.toString().trim();

        Lead lead = Lead.builder()
                .companyName(companyName)
                .contactPerson(cleanName)
                .email(cleanEmail)
                .phone(cleanPhone)
                .requirements(fullRequirements)
                .estimatedValue(request.getEstimatedBudget())
                .source(LeadSource.WEBSITE)
                .status(LeadStatus.NEW)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Lead saved = leadRepository.save(lead);

        // Generate optional WhatsApp connect URL for response payload
        String rawPhone = cleanPhone != null ? cleanPhone.replaceAll("[^0-9]", "") : "";
        String whatsappUrl = null;
        if (!rawPhone.isEmpty()) {
            String greetingText = URLEncoder.encode(
                    "Hello " + cleanName + ", thank you for reaching out to Webliix! We received your inquiry: \"" + fullRequirements + "\". Our team will be happy to assist you.",
                    StandardCharsets.UTF_8
            );
            whatsappUrl = "https://wa.me/" + rawPhone + "?text=" + greetingText;
        }

        // Publish domain event for decoupled email and internal notification processing
        eventPublisher.publishEvent(new com.webliix.crm.lead.event.PublicLeadSubmittedEvent(
                this,
                saved.getId(),
                saved.getContactPerson(),
                saved.getCompanyName(),
                saved.getEmail(),
                saved.getPhone(),
                saved.getRequirements(),
                request.getServiceRequested(),
                saved.getEstimatedValue(),
                request.getSource(),
                request.getPage(),
                saved.getCreatedAt()
        ));

        return PublicLeadResponse.builder()
                .leadId(saved.getId())
                .contactPerson(saved.getContactPerson())
                .companyName(saved.getCompanyName())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .status(saved.getStatus().name())
                .whatsappConnectUrl(whatsappUrl)
                .message("Thank you for contacting Webliix! We have received your inquiry and sent a confirmation email.")
                .createdAt(saved.getCreatedAt())
                .build();
    }

    private String sanitize(String input) {
        if (input == null) return "";
        return input.replaceAll("<[^>]*>", "").trim();
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

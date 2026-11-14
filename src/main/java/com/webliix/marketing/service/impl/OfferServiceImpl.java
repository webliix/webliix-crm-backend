package com.webliix.marketing.service.impl;

import com.webliix.marketing.dto.OfferRequest;
import com.webliix.marketing.dto.OfferResponse;
import com.webliix.marketing.entity.Offer;
import com.webliix.marketing.repository.OfferRepository;
import com.webliix.marketing.service.OfferService;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.annotation.PostConstruct;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;

    @PostConstruct
    public void seedDefaultOffersIfEmpty() {
        if (offerRepository.count() == 0) {
            offerRepository.save(Offer.builder()
                    .title("AI Integration & Workflow Copilot")
                    .description("Add generative AI search, automated customer intake assistants, and intelligent document indexing to your platform.")
                    .code("WEBLIIX-AI-25")
                    .discount("25% OFF Retainer")
                    .badge("Most Popular")
                    .badgeColor("primary")
                    .features("Custom LLM fine-tuning or RAG pipelines\nSeamless database integration\nStrict data privacy residency\nDedicated AI architect consultation")
                    .expiresAt("December 31, 2026")
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            offerRepository.save(Offer.builder()
                    .title("Cloud Infrastructure & DevOps Optimization")
                    .description("Audit your AWS, GCP, or Azure infrastructure for cost leaks, enhance CI/CD delivery pipelines, and configure auto-scaling.")
                    .code("CLOUD-OPTIM-30")
                    .discount("₹10,000 Credit")
                    .badge("Cost Saver")
                    .badgeColor("success")
                    .features("Infrastructure cost audit & reduction plan\nZero-downtime containerized CI/CD pipelines\nMulti-region automated disaster recovery\n24/7 cloud monitoring & telemetry alerts")
                    .expiresAt("December 31, 2026")
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            offerRepository.save(Offer.builder()
                    .title("Enterprise Security & Penetration Testing")
                    .description("Comprehensive vulnerability assessment, automated dependency scanning, and compliance roadmapping for enterprise readiness.")
                    .code("SEC-AUDIT-2026")
                    .discount("Free Compliance Review")
                    .badge("Enterprise Grade")
                    .badgeColor("secondary")
                    .features("Full OWASP Top 10 web & API pen test\nAutomated secret scanning & CVE mitigation\nExecutive security compliance report\nActionable remediation patch guidance")
                    .expiresAt("December 31, 2026")
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            offerRepository.save(Offer.builder()
                    .title("Cross-Platform Mobile App Companion")
                    .description("Extend your Webliix web application into iOS and Android with React Native / Flutter featuring native offline support.")
                    .code("MOBILE-EXPAND-20")
                    .discount("20% OFF Development")
                    .badge("New Expansion")
                    .badgeColor("warning")
                    .features("Shared backend APIs and authentication\nPush notification architecture via FCM/APNS\nApp Store & Google Play submission management\nCross-platform responsive design fidelity")
                    .expiresAt("December 31, 2026")
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfferResponse> getActiveOffers() {
        return offerRepository.findByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfferResponse> getAllOffers() {
        return offerRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OfferResponse getOffer(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + id));
        return toResponse(offer);
    }

    @Override
    @Transactional
    public OfferResponse createOffer(OfferRequest request) {
        Offer offer = Offer.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .code(request.getCode())
                .discount(request.getDiscount())
                .badge(request.getBadge())
                .badgeColor(request.getBadgeColor() != null ? request.getBadgeColor() : "primary")
                .features(request.getFeatures())
                .expiresAt(request.getExpiresAt())
                .active(request.getActive() != null ? request.getActive() : Boolean.TRUE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return toResponse(offerRepository.save(offer));
    }

    @Override
    @Transactional
    public OfferResponse updateOffer(Long id, OfferRequest request) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + id));
        offer.setTitle(request.getTitle());
        offer.setDescription(request.getDescription());
        offer.setCode(request.getCode());
        offer.setDiscount(request.getDiscount());
        offer.setBadge(request.getBadge());
        if (request.getBadgeColor() != null) {
            offer.setBadgeColor(request.getBadgeColor());
        }
        offer.setFeatures(request.getFeatures());
        offer.setExpiresAt(request.getExpiresAt());
        if (request.getActive() != null) {
            offer.setActive(request.getActive());
        }
        offer.setUpdatedAt(LocalDateTime.now());
        return toResponse(offerRepository.save(offer));
    }

    @Override
    @Transactional
    public OfferResponse toggleOffer(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + id));
        offer.setActive(!Boolean.TRUE.equals(offer.getActive()));
        offer.setUpdatedAt(LocalDateTime.now());
        return toResponse(offerRepository.save(offer));
    }

    @Override
    @Transactional
    public void deleteOffer(Long id) {
        if (!offerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Offer not found: " + id);
        }
        offerRepository.deleteById(id);
    }

    private OfferResponse toResponse(Offer offer) {
        OfferResponse res = new OfferResponse();
        res.setId(offer.getId());
        res.setTitle(offer.getTitle());
        res.setDescription(offer.getDescription());
        res.setCode(offer.getCode());
        res.setDiscount(offer.getDiscount());
        res.setBadge(offer.getBadge());
        res.setBadgeColor(offer.getBadgeColor());
        res.setFeatures(offer.getFeatures());
        res.setExpiresAt(offer.getExpiresAt());
        res.setActive(offer.getActive());
        res.setCreatedAt(offer.getCreatedAt());
        res.setUpdatedAt(offer.getUpdatedAt());
        return res;
    }
}

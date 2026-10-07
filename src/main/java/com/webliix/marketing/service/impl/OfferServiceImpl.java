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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;

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

package com.webliix.marketing.service;

import com.webliix.marketing.dto.OfferRequest;
import com.webliix.marketing.dto.OfferResponse;

import java.util.List;

public interface OfferService {
    List<OfferResponse> getActiveOffers();
    List<OfferResponse> getAllOffers();
    OfferResponse getOffer(Long id);
    OfferResponse createOffer(OfferRequest request);
    OfferResponse updateOffer(Long id, OfferRequest request);
    OfferResponse toggleOffer(Long id);
    void deleteOffer(Long id);
}

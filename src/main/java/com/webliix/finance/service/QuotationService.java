package com.webliix.finance.service;

import com.webliix.finance.dto.CreateQuotationRequest;
import com.webliix.finance.dto.QuotationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuotationService {

    QuotationResponse createQuotation(CreateQuotationRequest req);

    Page<QuotationResponse> getAllQuotations(Pageable pageable);

    QuotationResponse getQuotation(Long id);

    QuotationResponse updateQuotation(Long id, CreateQuotationRequest req);

    void deleteQuotation(Long id);
}

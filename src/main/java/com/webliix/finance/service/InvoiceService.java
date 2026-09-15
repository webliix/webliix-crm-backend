package com.webliix.finance.service;

import com.webliix.finance.dto.CreateInvoiceRequest;
import com.webliix.finance.dto.InvoiceDashboardResponse;
import com.webliix.finance.dto.InvoiceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InvoiceService {

    InvoiceResponse createInvoice(CreateInvoiceRequest req);

    Page<InvoiceResponse> getAllInvoices(Pageable pageable);

    InvoiceResponse getInvoice(Long id);

    InvoiceResponse updateInvoice(Long id, CreateInvoiceRequest req);

    void deleteInvoice(Long id);

    Page<InvoiceResponse> searchInvoices(String keyword, Pageable pageable);

    InvoiceDashboardResponse getDashboard();
}

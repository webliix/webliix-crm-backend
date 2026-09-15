package com.webliix.finance.service;

import com.webliix.finance.entity.Invoice;

public interface InvoiceConversionService {

    Invoice convertToInvoice(Long quotationId);
}

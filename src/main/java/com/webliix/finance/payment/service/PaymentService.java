package com.webliix.finance.payment.service;

import com.webliix.finance.payment.dto.PaymentRequest;
import com.webliix.finance.payment.dto.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentResponse recordPayment(Long invoiceId, PaymentRequest request);
    Page<PaymentResponse> getAllPayments(Pageable pageable);
    PaymentResponse getPayment(Long id);
    Page<PaymentResponse> getPaymentsByCustomer(Long customerId, Pageable pageable);
    Page<PaymentResponse> getPaymentsByInvoice(Long invoiceId, Pageable pageable);
}

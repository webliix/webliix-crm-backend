package com.webliix.finance.payment.mapper;

import com.webliix.finance.payment.dto.PaymentResponse;
import com.webliix.finance.payment.entity.Payment;

public class PaymentMapper {

    public static PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setPaymentNumber(payment.getPaymentNumber());
        if (payment.getInvoice() != null) {
            response.setInvoiceId(payment.getInvoice().getId());
        }
        if (payment.getCustomer() != null) {
            response.setCustomerId(payment.getCustomer().getId());
        }
        response.setAmount(payment.getAmount());
        response.setPaymentDate(payment.getPaymentDate());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus());
        response.setTransactionReference(payment.getTransactionReference());
        response.setRemarks(payment.getRemarks());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }
}

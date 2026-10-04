package com.webliix.hr.paymentsubmission.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentSubmissionRequest {
    private Long projectId;
    private Long customerId;
    private BigDecimal amount;
    private String currency;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String referenceNumber;
    private String payerName;
    private String receiverDetails;
    private String notes;
    private Long linkedInvoiceId;
}

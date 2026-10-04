package com.webliix.hr.paymentsubmission.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaymentSubmissionResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long projectId;
    private String projectName;
    private Long customerId;
    private String customerName;
    private BigDecimal amount;
    private String currency;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String referenceNumber;
    private String payerName;
    private String receiverDetails;
    private String notes;
    private String status;
    private String reviewedBy;
    private String reviewNotes;
    private LocalDateTime reviewedAt;
    private Long linkedInvoiceId;
    private String linkedInvoiceNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

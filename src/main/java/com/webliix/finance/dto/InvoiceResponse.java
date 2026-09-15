package com.webliix.finance.dto;

import com.webliix.finance.enums.InvoiceStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private Long customerId;
    private Long projectId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private InvoiceStatus status;
    private String notes;
    private List<InvoiceItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

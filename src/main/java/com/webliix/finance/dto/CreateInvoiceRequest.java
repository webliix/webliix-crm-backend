package com.webliix.finance.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateInvoiceRequest {
    private Long customerId;
    private Long projectId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private List<CreateInvoiceItemRequest> items;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private String notes;
}

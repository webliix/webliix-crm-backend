package com.webliix.finance.dto;

import com.webliix.finance.enums.QuotationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuotationResponse {
    private Long id;
    private String quotationNumber;
    private Long customerId;
    private Long projectId;
    private LocalDate issueDate;
    private LocalDate validTill;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    private QuotationStatus status;
    private String notes;
    private List<QuotationItemResponse> items;
    private Boolean converted;
    private LocalDateTime convertedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

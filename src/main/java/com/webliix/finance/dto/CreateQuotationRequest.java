package com.webliix.finance.dto;

import com.webliix.finance.enums.QuotationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateQuotationRequest {
    private Long customerId;
    private Long projectId;
    private LocalDate issueDate;
    private LocalDate validTill;
    private List<CreateQuotationItemRequest> items;
    private BigDecimal taxAmount;
    private BigDecimal discount;
    private QuotationStatus status;
    private String notes;
}

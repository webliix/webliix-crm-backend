package com.webliix.finance.dto;

import com.webliix.finance.enums.ExpenseCategory;
import com.webliix.finance.enums.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseRequest {
    private ExpenseCategory category;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private PaymentMethod paymentMethod;
    private String vendor;
    private String createdBy;
}

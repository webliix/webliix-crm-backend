package com.webliix.finance.dto;

import com.webliix.finance.enums.ExpenseCategory;
import com.webliix.finance.enums.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExpenseResponse {
    private Long id;
    private String expenseNumber;
    private ExpenseCategory category;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private PaymentMethod paymentMethod;
    private String vendor;
    private String createdBy;
    private LocalDateTime createdAt;
}

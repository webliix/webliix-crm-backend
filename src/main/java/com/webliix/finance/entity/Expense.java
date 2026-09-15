package com.webliix.finance.entity;

import com.webliix.finance.enums.ExpenseCategory;
import com.webliix.finance.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "expense_number", unique = true, nullable = false)
    private String expenseNumber;

    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal amount;

    @Column(name = "expense_date")
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    private String vendor;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

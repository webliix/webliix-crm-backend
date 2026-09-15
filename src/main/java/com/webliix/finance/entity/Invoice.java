package com.webliix.finance.entity;

import com.webliix.crm.customer.entity.Customer;
import com.webliix.projects.entity.Project;
import com.webliix.finance.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", unique = true, nullable = false)
    private String invoiceNumber;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    private BigDecimal subtotal;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "paid_amount")
    private BigDecimal paidAmount;

    @Column(name = "pending_amount")
    private BigDecimal pendingAmount;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

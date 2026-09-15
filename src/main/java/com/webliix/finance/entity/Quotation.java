package com.webliix.finance.entity;

import com.webliix.finance.enums.QuotationStatus;
import com.webliix.crm.customer.entity.Customer;
import com.webliix.projects.entity.Project;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quotations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quotation_number", unique = true, nullable = false)
    private String quotationNumber;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "valid_till")
    private LocalDate validTill;

    private BigDecimal subtotal;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount;

    private BigDecimal discount;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private QuotationStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuotationItem> items;

    private Boolean converted;

    @Column(name = "converted_at")
    private LocalDateTime convertedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

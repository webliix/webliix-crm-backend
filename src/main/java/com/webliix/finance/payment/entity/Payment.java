package com.webliix.finance.payment.entity;

import com.webliix.crm.customer.entity.Customer;
import com.webliix.finance.enums.PaymentMethod;
import com.webliix.finance.enums.PaymentStatus;
import com.webliix.finance.entity.Invoice;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_number", unique = true, nullable = false)
    private String paymentNumber;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private BigDecimal amount;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "transaction_reference")
    private String transactionReference;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

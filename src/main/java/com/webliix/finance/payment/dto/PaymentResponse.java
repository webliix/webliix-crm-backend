package com.webliix.finance.payment.dto;

import com.webliix.finance.enums.PaymentMethod;
import com.webliix.finance.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private String paymentNumber;
    private Long invoiceId;
    private Long customerId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionReference;
    private String remarks;
    private LocalDateTime createdAt;
}

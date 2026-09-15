package com.webliix.finance.payment.dto;

import com.webliix.finance.enums.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentRequest {
    private Long invoiceId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private String transactionReference;
    private String remarks;
}

package com.webliix.finance.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceItemResponse {
    private Long id;
    private String itemName;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}

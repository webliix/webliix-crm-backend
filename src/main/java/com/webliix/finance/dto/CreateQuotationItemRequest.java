package com.webliix.finance.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateQuotationItemRequest {
    private String itemName;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
}

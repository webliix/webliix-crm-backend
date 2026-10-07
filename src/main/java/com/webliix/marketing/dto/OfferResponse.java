package com.webliix.marketing.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OfferResponse {
    private Long id;
    private String title;
    private String description;
    private String code;
    private String discount;
    private String badge;
    private String badgeColor;
    private String features;
    private String expiresAt;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

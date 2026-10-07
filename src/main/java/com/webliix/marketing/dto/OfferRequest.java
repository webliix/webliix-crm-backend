package com.webliix.marketing.dto;

import lombok.Data;

@Data
public class OfferRequest {
    private String title;
    private String description;
    private String code;
    private String discount;
    private String badge;
    private String badgeColor;
    private String features;
    private String expiresAt;
    private Boolean active;
}

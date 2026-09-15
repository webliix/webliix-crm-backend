package com.webliix.billing.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_per_month")
    private BigDecimal pricePerMonth;

    @Column(name = "price_per_year")
    private BigDecimal pricePerYear;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "max_storage_gb")
    private Integer maxStorageGb;

    @Column(name = "max_api_calls_per_month")
    private Long maxApiCallsPerMonth;

    @Column(columnDefinition = "TEXT")
    private String features; // JSON array of features

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

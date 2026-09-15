package com.webliix.billing.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webliix.billing.dto.PlanResponse;
import com.webliix.billing.dto.SubscriptionRequest;
import com.webliix.billing.dto.SubscriptionResponse;
import com.webliix.billing.entity.Plan;
import com.webliix.billing.entity.Subscription;
import com.webliix.billing.repository.PlanRepository;
import com.webliix.billing.repository.SubscriptionRepository;
import com.webliix.shared.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;

    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<Plan>>> getPlans() {
        List<Plan> plans = planRepository.findAll();
        return ResponseEntity.ok(
                ApiResponse.<List<Plan>>builder()
                        .success(true)
                        .message("Plans fetched")
                        .data(plans)
                        .build()
        );
    }

    @GetMapping("/plans/{id}")
    public ResponseEntity<ApiResponse<Plan>> getPlan(@PathVariable Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        return ResponseEntity.ok(
                ApiResponse.<Plan>builder()
                        .success(true)
                        .message("Plan fetched")
                        .data(plan)
                        .build()
        );
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<ApiResponse<Subscription>> createSubscription(@RequestBody SubscriptionRequest request) {
        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        Subscription subscription = Subscription.builder()
                .tenantId(request.getTenantId())
                .planId(plan.getId())
                .status("ACTIVE")
                .startedAt(java.time.LocalDateTime.now())
                .expiresAt(java.time.LocalDateTime.now().plusMonths(1))
                .nextBillingDate(java.time.LocalDateTime.now().plusMonths(1))
                .autoRenew(true)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();

        subscriptionRepository.save(subscription);
        return ResponseEntity.ok(
                ApiResponse.<Subscription>builder()
                        .success(true)
                        .message("Subscription created")
                        .data(subscription)
                        .build()
        );
    }

    @GetMapping("/subscriptions/{tenantId}")
    public ResponseEntity<ApiResponse<Subscription>> getSubscription(@PathVariable Long tenantId) {
        Subscription subscription = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        return ResponseEntity.ok(
                ApiResponse.<Subscription>builder()
                        .success(true)
                        .message("Subscription fetched")
                        .data(subscription)
                        .build()
        );
    }
}

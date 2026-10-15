package com.webliix.hr.paymentsubmission.controller;

import com.webliix.hr.paymentsubmission.dto.PaymentSubmissionRequest;
import com.webliix.hr.paymentsubmission.dto.PaymentSubmissionResponse;
import com.webliix.hr.paymentsubmission.dto.PaymentSubmissionReviewRequest;
import com.webliix.hr.paymentsubmission.service.PaymentSubmissionService;
import com.webliix.security.annotation.RequirePermission;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentSubmissionController {

    private final PaymentSubmissionService paymentSubmissionService;

    // === EMPLOYEE SELF-SERVICE ROUTES ===

    @PostMapping("/employee/me/payment-submissions")
    @RequirePermission("PAYMENT_SUBMISSIONS_SUBMIT")
    public ResponseEntity<ApiResponse<PaymentSubmissionResponse>> submitPayment(
            @RequestBody PaymentSubmissionRequest request,
            Authentication auth) {
        PaymentSubmissionResponse response = paymentSubmissionService.submitPayment(request, auth);
        return ResponseEntity.ok(ApiResponse.<PaymentSubmissionResponse>builder()
                .success(true).message("Payment submission recorded successfully").data(response).build());
    }

    @GetMapping("/employee/me/payment-submissions")
    @RequirePermission(anyOf = {"PAYMENT_SUBMISSIONS_SUBMIT", "PAYMENT_SUBMISSIONS_VIEW"})
    public ResponseEntity<ApiResponse<Page<PaymentSubmissionResponse>>> getMySubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentSubmissionResponse> response = paymentSubmissionService.getMySubmissions(pageable, auth);
        return ResponseEntity.ok(ApiResponse.<Page<PaymentSubmissionResponse>>builder()
                .success(true).message("Payment submissions fetched successfully").data(response).build());
    }

    // === ADMIN/FINANCE REVIEW ROUTES ===

    @GetMapping("/payment-submissions")
    @RequirePermission("PAYMENT_SUBMISSIONS_VIEW")
    public ResponseEntity<ApiResponse<Page<PaymentSubmissionResponse>>> getAllSubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentSubmissionResponse> response = status != null
                ? paymentSubmissionService.getSubmissionsByStatus(status, pageable)
                : paymentSubmissionService.getAllSubmissions(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<PaymentSubmissionResponse>>builder()
                .success(true).message("All payment submissions fetched successfully").data(response).build());
    }

    @GetMapping("/payment-submissions/{id}")
    @RequirePermission("PAYMENT_SUBMISSIONS_VIEW")
    public ResponseEntity<ApiResponse<PaymentSubmissionResponse>> getSubmission(
            @PathVariable Long id,
            Authentication auth) {
        PaymentSubmissionResponse response = paymentSubmissionService.getSubmission(id, auth);
        return ResponseEntity.ok(ApiResponse.<PaymentSubmissionResponse>builder()
                .success(true).message("Payment submission fetched successfully").data(response).build());
    }

    @PatchMapping("/payment-submissions/{id}/review")
    @RequirePermission("PAYMENT_SUBMISSIONS_REVIEW")
    public ResponseEntity<ApiResponse<PaymentSubmissionResponse>> reviewSubmission(
            @PathVariable Long id,
            @RequestBody PaymentSubmissionReviewRequest request,
            Authentication auth) {
        PaymentSubmissionResponse response = paymentSubmissionService.reviewSubmission(id, request, auth);
        return ResponseEntity.ok(ApiResponse.<PaymentSubmissionResponse>builder()
                .success(true).message("Payment submission review saved").data(response).build());
    }

    @DeleteMapping("/payment-submissions/{id}")
    @RequirePermission(anyOf = {"PAYMENT_SUBMISSIONS_SUBMIT", "PAYMENT_SUBMISSIONS_VIEW"})
    public ResponseEntity<ApiResponse<Void>> deleteSubmission(
            @PathVariable Long id,
            Authentication auth) {
        paymentSubmissionService.deleteSubmission(id, auth);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Payment submission deleted successfully").build());
    }
}

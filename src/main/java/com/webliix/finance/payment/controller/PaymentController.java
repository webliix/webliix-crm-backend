package com.webliix.finance.payment.controller;

import com.webliix.finance.payment.dto.PaymentRequest;
import com.webliix.finance.payment.dto.PaymentResponse;
import com.webliix.finance.payment.service.PaymentService;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/payments")
    public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.recordPayment(request.getInvoiceId(), request);
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder().success(true).message("Payment recorded successfully").data(response).build());
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getPayments(@RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentResponse> result = paymentService.getAllPayments(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<PaymentResponse>>builder().success(true).message("Payments fetched").data(result).build());
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable Long id) {
        PaymentResponse response = paymentService.getPayment(id);
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder().success(true).message("Payment fetched").data(response).build());
    }

    @GetMapping("/customers/{customerId}/payments")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getCustomerPayments(@PathVariable Long customerId,
                                                                                 @RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentResponse> result = paymentService.getPaymentsByCustomer(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<PaymentResponse>>builder().success(true).message("Customer payments fetched").data(result).build());
    }

    @GetMapping("/invoices/{invoiceId}/payments")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getInvoicePayments(@PathVariable Long invoiceId,
                                                                                @RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentResponse> result = paymentService.getPaymentsByInvoice(invoiceId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<PaymentResponse>>builder().success(true).message("Invoice payments fetched").data(result).build());
    }
}

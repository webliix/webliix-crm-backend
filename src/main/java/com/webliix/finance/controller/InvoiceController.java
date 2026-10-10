package com.webliix.finance.controller;

import com.webliix.finance.dto.CreateInvoiceRequest;
import com.webliix.finance.dto.InvoiceDashboardResponse;
import com.webliix.finance.dto.InvoiceResponse;
import com.webliix.finance.service.InvoiceService;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final com.webliix.finance.payment.repository.PaymentRepository paymentRepository;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('INVOICES_CREATE', 'ADMIN', 'ROLE_ADMIN', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'MANAGER', 'ROLE_MANAGER', 'EMPLOYEE', 'ROLE_EMPLOYEE')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> create(@RequestBody CreateInvoiceRequest req) {
        InvoiceResponse response = invoiceService.createInvoice(req);
        return ResponseEntity.ok(ApiResponse.<InvoiceResponse>builder().success(true).message("Invoice created successfully").data(response).build());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InvoiceResponse> result = invoiceService.getAllInvoices(projectId, customerId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<InvoiceResponse>>builder().success(true).message("Invoices fetched").data(result).build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<InvoiceResponse>> get(@PathVariable Long id) {
        InvoiceResponse response = invoiceService.getInvoice(id);
        return ResponseEntity.ok(ApiResponse.<InvoiceResponse>builder().success(true).message("Invoice fetched").data(response).build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('INVOICES_EDIT', 'ADMIN', 'ROLE_ADMIN', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'MANAGER', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> update(@PathVariable Long id, @RequestBody CreateInvoiceRequest req) {
        InvoiceResponse response = invoiceService.updateInvoice(id, req);
        return ResponseEntity.ok(ApiResponse.<InvoiceResponse>builder().success(true).message("Invoice updated").data(response).build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('INVOICES_DELETE', 'ADMIN', 'ROLE_ADMIN', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Invoice deleted").build());
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> search(@RequestParam String keyword,
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InvoiceResponse> result = invoiceService.searchInvoices(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<InvoiceResponse>>builder().success(true).message("Search results").data(result).build());
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'MANAGER', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<InvoiceDashboardResponse>> dashboard() {
        InvoiceDashboardResponse response = invoiceService.getDashboard();
        return ResponseEntity.ok(ApiResponse.<InvoiceDashboardResponse>builder().success(true).message("Invoice dashboard").data(response).build());
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'MANAGER', 'ROLE_MANAGER', 'INVOICES_EDIT', 'INVOICES_CREATE')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> recordPayment(
            @PathVariable Long id,
            @RequestBody com.webliix.finance.dto.RecordPaymentRequest req) {
        InvoiceResponse response = invoiceService.recordPayment(id, req);
        return ResponseEntity.ok(ApiResponse.<InvoiceResponse>builder()
                .success(true)
                .message("Payment recorded and applied to invoice successfully")
                .data(response)
                .build());
    }

    @GetMapping("/{id}/payments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<java.util.List<com.webliix.finance.payment.entity.Payment>>> getPayments(@PathVariable Long id) {
        java.util.List<com.webliix.finance.payment.entity.Payment> payments = paymentRepository.findByInvoiceId(id);
        return ResponseEntity.ok(ApiResponse.<java.util.List<com.webliix.finance.payment.entity.Payment>>builder()
                .success(true)
                .message("Payments fetched")
                .data(payments)
                .build());
    }
}

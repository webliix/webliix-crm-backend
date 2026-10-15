package com.webliix.finance.controller;

import com.webliix.finance.dto.CreateInvoiceRequest;
import com.webliix.finance.dto.InvoiceDashboardResponse;
import com.webliix.finance.dto.InvoiceResponse;
import com.webliix.finance.payment.entity.Payment;
import com.webliix.finance.payment.repository.PaymentRepository;
import com.webliix.finance.service.InvoiceService;
import com.webliix.security.annotation.RequirePermission;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PaymentRepository paymentRepository;

    @PostMapping
    @RequirePermission("INVOICES_CREATE")
    public ResponseEntity<ApiResponse<InvoiceResponse>> create(@RequestBody CreateInvoiceRequest req) {
        InvoiceResponse response = invoiceService.createInvoice(req);
        return ResponseEntity.ok(ApiResponse.<InvoiceResponse>builder().success(true).message("Invoice created successfully").data(response).build());
    }

    @GetMapping
    @RequirePermission("INVOICES_VIEW")
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
    @RequirePermission("INVOICES_VIEW")
    public ResponseEntity<ApiResponse<InvoiceResponse>> get(@PathVariable Long id) {
        InvoiceResponse response = invoiceService.getInvoice(id);
        return ResponseEntity.ok(ApiResponse.<InvoiceResponse>builder().success(true).message("Invoice fetched").data(response).build());
    }

    @PutMapping("/{id}")
    @RequirePermission("INVOICES_EDIT")
    public ResponseEntity<ApiResponse<InvoiceResponse>> update(@PathVariable Long id, @RequestBody CreateInvoiceRequest req) {
        InvoiceResponse response = invoiceService.updateInvoice(id, req);
        return ResponseEntity.ok(ApiResponse.<InvoiceResponse>builder().success(true).message("Invoice updated").data(response).build());
    }

    @DeleteMapping("/{id}")
    @RequirePermission("INVOICES_DELETE")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Invoice deleted").build());
    }

    @GetMapping("/search")
    @RequirePermission("INVOICES_VIEW")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> search(@RequestParam String keyword,
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InvoiceResponse> result = invoiceService.searchInvoices(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<InvoiceResponse>>builder().success(true).message("Search results").data(result).build());
    }

    @GetMapping("/dashboard")
    @RequirePermission("INVOICES_VIEW")
    public ResponseEntity<ApiResponse<InvoiceDashboardResponse>> dashboard() {
        InvoiceDashboardResponse response = invoiceService.getDashboard();
        return ResponseEntity.ok(ApiResponse.<InvoiceDashboardResponse>builder().success(true).message("Invoice dashboard").data(response).build());
    }

    @PostMapping("/{id}/payments")
    @RequirePermission("PAYMENTS_RECORD")
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
    @RequirePermission("PAYMENTS_VIEW")
    public ResponseEntity<ApiResponse<List<Payment>>> getPayments(@PathVariable Long id) {
        List<Payment> payments = paymentRepository.findByInvoiceId(id);
        return ResponseEntity.ok(ApiResponse.<List<Payment>>builder()
                .success(true)
                .message("Payments fetched")
                .data(payments)
                .build());
    }
}

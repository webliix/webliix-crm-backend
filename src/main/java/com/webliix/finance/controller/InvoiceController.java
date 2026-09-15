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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceResponse>> create(@RequestBody CreateInvoiceRequest req) {
        InvoiceResponse response = invoiceService.createInvoice(req);
        return ResponseEntity.ok(ApiResponse.<InvoiceResponse>builder().success(true).message("Invoice created successfully").data(response).build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> list(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InvoiceResponse> result = invoiceService.getAllInvoices(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<InvoiceResponse>>builder().success(true).message("Invoices fetched").data(result).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> get(@PathVariable Long id) {
        InvoiceResponse response = invoiceService.getInvoice(id);
        return ResponseEntity.ok(ApiResponse.<InvoiceResponse>builder().success(true).message("Invoice fetched").data(response).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> update(@PathVariable Long id, @RequestBody CreateInvoiceRequest req) {
        InvoiceResponse response = invoiceService.updateInvoice(id, req);
        return ResponseEntity.ok(ApiResponse.<InvoiceResponse>builder().success(true).message("Invoice updated").data(response).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Invoice deleted").build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> search(@RequestParam String keyword,
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InvoiceResponse> result = invoiceService.searchInvoices(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<InvoiceResponse>>builder().success(true).message("Search results").data(result).build());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<InvoiceDashboardResponse>> dashboard() {
        InvoiceDashboardResponse response = invoiceService.getDashboard();
        return ResponseEntity.ok(ApiResponse.<InvoiceDashboardResponse>builder().success(true).message("Invoice dashboard").data(response).build());
    }
}

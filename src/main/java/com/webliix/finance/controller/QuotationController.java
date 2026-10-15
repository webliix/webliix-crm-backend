package com.webliix.finance.controller;

import com.webliix.finance.dto.CreateQuotationRequest;
import com.webliix.finance.dto.QuotationResponse;
import com.webliix.finance.service.InvoiceConversionService;
import com.webliix.finance.service.QuotationService;
import com.webliix.security.annotation.RequirePermission;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quotations")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;
    private final InvoiceConversionService conversionService;

    @PostMapping
    @RequirePermission("QUOTATIONS_CREATE")
    public ResponseEntity<ApiResponse<QuotationResponse>> create(@RequestBody CreateQuotationRequest req) {
        QuotationResponse res = quotationService.createQuotation(req);
        return ResponseEntity.ok(ApiResponse.<QuotationResponse>builder().success(true).message("Created").data(res).build());
    }

    @GetMapping
    @RequirePermission("QUOTATIONS_VIEW")
    public ResponseEntity<ApiResponse<Page<QuotationResponse>>> list(@RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuotationResponse> res = quotationService.getAllQuotations(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<QuotationResponse>>builder().success(true).message("List").data(res).build());
    }

    @GetMapping("/{id}")
    @RequirePermission("QUOTATIONS_VIEW")
    public ResponseEntity<ApiResponse<QuotationResponse>> get(@PathVariable Long id) {
        QuotationResponse res = quotationService.getQuotation(id);
        return ResponseEntity.ok(ApiResponse.<QuotationResponse>builder().success(true).message("Found").data(res).build());
    }

    @PutMapping("/{id}")
    @RequirePermission("QUOTATIONS_EDIT")
    public ResponseEntity<ApiResponse<QuotationResponse>> update(@PathVariable Long id, @RequestBody CreateQuotationRequest req) {
        QuotationResponse res = quotationService.updateQuotation(id, req);
        return ResponseEntity.ok(ApiResponse.<QuotationResponse>builder().success(true).message("Updated").data(res).build());
    }

    @DeleteMapping("/{id}")
    @RequirePermission("QUOTATIONS_DELETE")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        quotationService.deleteQuotation(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Deleted").build());
    }

    @PostMapping("/{id}/convert")
    @RequirePermission("QUOTATIONS_CONVERT")
    public ResponseEntity<ApiResponse<Void>> convertToInvoice(@PathVariable Long id) {
        conversionService.convertToInvoice(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Invoice generated successfully")
                .build());
    }
}

package com.webliix.crm.lead.controller;

import com.webliix.crm.lead.dto.CreateLeadRequest;
import com.webliix.crm.lead.dto.LeadResponse;
import com.webliix.crm.lead.service.LeadConversionService;
import com.webliix.crm.lead.service.LeadService;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;
    private final LeadConversionService leadConversionService;

    @PostMapping
    public ResponseEntity<ApiResponse<LeadResponse>> createLead(@RequestBody CreateLeadRequest request) {
        LeadResponse res = leadService.createLead(request);
        return ResponseEntity.ok(ApiResponse.<LeadResponse>builder()
                .success(true)
                .message("Lead created successfully")
                .data(res)
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<LeadResponse>>> getAllLeads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LeadResponse> res = leadService.getAllLeads(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<LeadResponse>>builder()
                .success(true)
                .message("Leads fetched")
                .data(res)
                .build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<LeadResponse>>> searchLeads(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LeadResponse> res = leadService.searchLeads(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<LeadResponse>>builder()
                .success(true)
                .message("Search results")
                .data(res)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadResponse>> getLead(@PathVariable Long id) {
        LeadResponse res = leadService.getLead(id);
        return ResponseEntity.ok(ApiResponse.<LeadResponse>builder()
                .success(true)
                .message("Lead fetched")
                .data(res)
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadResponse>> updateLead(@PathVariable Long id, @RequestBody CreateLeadRequest request) {
        LeadResponse res = leadService.updateLead(id, request);
        return ResponseEntity.ok(ApiResponse.<LeadResponse>builder()
                .success(true)
                .message("Lead updated")
                .data(res)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Lead deleted")
                .build());
    }

    @PostMapping("/{id}/convert")
    public ResponseEntity<ApiResponse<Void>> convertLead(@PathVariable Long id) {
        leadConversionService.convertLead(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Lead converted successfully")
                .build());
    }
}

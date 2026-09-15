package com.webliix.crm.lead.controller;

import com.webliix.crm.lead.dto.PublicLeadRequest;
import com.webliix.crm.lead.dto.PublicLeadResponse;
import com.webliix.crm.lead.service.LeadService;
import com.webliix.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/leads")
@RequiredArgsConstructor
public class PublicLeadController {

    private final LeadService leadService;

    @PostMapping
    public ResponseEntity<ApiResponse<PublicLeadResponse>> capturePublicLead(@Valid @RequestBody PublicLeadRequest request) {
        PublicLeadResponse response = leadService.createPublicLead(request);
        return ResponseEntity.ok(ApiResponse.<PublicLeadResponse>builder()
                .success(true)
                .message("Thank you for contacting Webliix! We have received your inquiry and sent a confirmation to your email.")
                .data(response)
                .build());
    }
}

package com.webliix.crm.lead.controller;

import com.webliix.crm.lead.dto.PublicLeadRequest;
import com.webliix.crm.lead.dto.PublicLeadResponse;
import com.webliix.crm.lead.service.LeadService;
import com.webliix.security.service.RateLimiterService;
import com.webliix.shared.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping({"/api/v1/public/leads", "/api/public/leads"})
@RequiredArgsConstructor
public class PublicLeadController {

    private final LeadService leadService;
    private final RateLimiterService rateLimiterService;

    @PostMapping
    public ResponseEntity<ApiResponse<PublicLeadResponse>> capturePublicLead(
            @Valid @RequestBody PublicLeadRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = getClientIp(httpRequest);

        // 1. Server-side Rate Limiting (10 form submissions per 15 mins per IP/email)
        rateLimiterService.checkRateLimit("public-lead:" + clientIp, 10, 900, "lead inquiry submission");
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            rateLimiterService.checkRateLimit("public-lead-email:" + request.getEmail().trim().toLowerCase(), 5, 900, "lead inquiry submission");
        }

        // 2. Anti-Spam / Honeypot trap validation
        if (request.getHoneypot() != null && !request.getHoneypot().isBlank()) {
            log.warn("Spam bot submission blocked via honeypot field from IP: {}", clientIp);
            PublicLeadResponse fakeResponse = PublicLeadResponse.builder()
                    .leadId(0L)
                    .contactPerson(request.getName())
                    .email(request.getEmail())
                    .status("NEW")
                    .message("Thank you for contacting Webliix! We have received your inquiry.")
                    .createdAt(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(ApiResponse.<PublicLeadResponse>builder()
                    .success(true)
                    .message("Thank you for contacting Webliix!")
                    .data(fakeResponse)
                    .build());
        }

        // 3. Delegate lead processing to service (which publishes PublicLeadSubmittedEvent)
        PublicLeadResponse response = leadService.createPublicLead(request);

        return ResponseEntity.ok(ApiResponse.<PublicLeadResponse>builder()
                .success(true)
                .message("Thank you for contacting Webliix! We have received your inquiry and sent a confirmation email.")
                .data(response)
                .build());
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
        }
        return xfHeader.split(",")[0].trim();
    }
}

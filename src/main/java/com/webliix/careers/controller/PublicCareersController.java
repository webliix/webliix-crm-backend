package com.webliix.careers.controller;

import com.webliix.careers.dto.JobApplicationDto;
import com.webliix.careers.dto.JobOpeningDto;
import com.webliix.careers.dto.SubmitJobApplicationRequest;
import com.webliix.careers.service.JobService;
import com.webliix.security.service.RateLimiterService;
import com.webliix.shared.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping({"/api/v1/public/careers", "/api/public/careers"})
@RequiredArgsConstructor
public class PublicCareersController {

    private final JobService jobService;
    private final RateLimiterService rateLimiterService;

    @GetMapping("/openings")
    public ResponseEntity<ApiResponse<List<JobOpeningDto>>> getActiveOpenings() {
        List<JobOpeningDto> openings = jobService.getActiveOpenings();
        return ResponseEntity.ok(ApiResponse.<List<JobOpeningDto>>builder()
                .success(true)
                .message("Active job openings retrieved")
                .data(openings)
                .build());
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<JobApplicationDto>> apply(
            @Valid @RequestBody SubmitJobApplicationRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);

        // Rate limit: 5 applications per 15 minutes per IP
        rateLimiterService.checkRateLimit("career-apply-ip:" + clientIp, 5, 900, "career application");
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            rateLimiterService.checkRateLimit("career-apply-email:" + request.getEmail().trim().toLowerCase(), 3, 900, "career application");
        }

        JobApplicationDto response = jobService.submitApplication(request);

        return ResponseEntity.ok(ApiResponse.<JobApplicationDto>builder()
                .success(true)
                .message("Thank you! Your application for " + request.getJobTitle() + " has been received.")
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

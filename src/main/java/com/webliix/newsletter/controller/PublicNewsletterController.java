package com.webliix.newsletter.controller;

import com.webliix.newsletter.dto.NewsletterSubscribeRequest;
import com.webliix.newsletter.dto.NewsletterSubscriberResponse;
import com.webliix.newsletter.service.NewsletterService;
import com.webliix.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/newsletter")
@RequiredArgsConstructor
public class PublicNewsletterController {

    private final NewsletterService newsletterService;

    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<NewsletterSubscriberResponse>> subscribe(
            @Valid @RequestBody NewsletterSubscribeRequest request
    ) {
        NewsletterSubscriberResponse response = newsletterService.subscribe(request);
        return ResponseEntity.ok(ApiResponse.<NewsletterSubscriberResponse>builder()
                .success(true)
                .message(response.getMessage())
                .data(response)
                .build());
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(@RequestParam String token) {
        newsletterService.unsubscribe(token);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("You have been unsubscribed successfully.")
                .build());
    }
}

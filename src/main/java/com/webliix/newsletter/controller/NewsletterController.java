package com.webliix.newsletter.controller;

import com.webliix.newsletter.dto.NewsletterSubscriberResponse;
import com.webliix.newsletter.service.NewsletterService;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterService newsletterService;

    @GetMapping("/subscribers")
    public ResponseEntity<ApiResponse<Page<NewsletterSubscriberResponse>>> getSubscribers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("subscribedAt").descending());
        Page<NewsletterSubscriberResponse> response = newsletterService.getAllSubscribers(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<NewsletterSubscriberResponse>>builder()
                .success(true)
                .message("Subscribers fetched")
                .data(response)
                .build());
    }
}

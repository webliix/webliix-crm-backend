package com.webliix.marketing.controller;

import com.webliix.marketing.dto.OfferRequest;
import com.webliix.marketing.dto.OfferResponse;
import com.webliix.marketing.service.OfferService;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    /**
     * GET /api/v1/offers
     * Returns offers. Clients/public get active offers; Staff can specify ?all=true to see all.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OfferResponse>>> getOffers(
            @RequestParam(defaultValue = "false") boolean all
    ) {
        List<OfferResponse> response = all
                ? offerService.getAllOffers()
                : offerService.getActiveOffers();
        return ResponseEntity.ok(ApiResponse.<List<OfferResponse>>builder()
                .success(true)
                .message("Offers fetched successfully")
                .data(response)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OfferResponse>> getOffer(@PathVariable Long id) {
        OfferResponse response = offerService.getOffer(id);
        return ResponseEntity.ok(ApiResponse.<OfferResponse>builder()
                .success(true)
                .message("Offer fetched successfully")
                .data(response)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'MANAGER', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<OfferResponse>> createOffer(@RequestBody OfferRequest request) {
        OfferResponse response = offerService.createOffer(request);
        return ResponseEntity.ok(ApiResponse.<OfferResponse>builder()
                .success(true)
                .message("Offer created successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'MANAGER', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<OfferResponse>> updateOffer(
            @PathVariable Long id,
            @RequestBody OfferRequest request
    ) {
        OfferResponse response = offerService.updateOffer(id, request);
        return ResponseEntity.ok(ApiResponse.<OfferResponse>builder()
                .success(true)
                .message("Offer updated successfully")
                .data(response)
                .build());
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'MANAGER', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<OfferResponse>> toggleOffer(@PathVariable Long id) {
        OfferResponse response = offerService.toggleOffer(id);
        return ResponseEntity.ok(ApiResponse.<OfferResponse>builder()
                .success(true)
                .message("Offer status toggled")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'MANAGER', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Offer deleted successfully")
                .build());
    }
}

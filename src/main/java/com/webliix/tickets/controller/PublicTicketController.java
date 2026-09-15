package com.webliix.tickets.controller;

import com.webliix.shared.response.ApiResponse;
import com.webliix.tickets.dto.CreateTicketCommentRequest;
import com.webliix.tickets.dto.PublicTicketDetailsResponse;
import com.webliix.tickets.dto.PublicTicketRequest;
import com.webliix.tickets.dto.TicketCommentResponse;
import com.webliix.tickets.dto.TicketResponse;
import com.webliix.tickets.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/tickets")
@RequiredArgsConstructor
public class PublicTicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> createPublicTicket(@Valid @RequestBody PublicTicketRequest request) {
        TicketResponse response = ticketService.createPublicTicket(request);
        return ResponseEntity.ok(ApiResponse.<TicketResponse>builder()
                .success(true)
                .message("Ticket created successfully. Our team will review your query shortly.")
                .data(response)
                .build());
    }

    @GetMapping("/{ticketNumber}")
    public ResponseEntity<ApiResponse<PublicTicketDetailsResponse>> getPublicTicket(@PathVariable String ticketNumber) {
        PublicTicketDetailsResponse response = ticketService.getPublicTicket(ticketNumber);
        return ResponseEntity.ok(ApiResponse.<PublicTicketDetailsResponse>builder()
                .success(true)
                .message("Ticket details retrieved")
                .data(response)
                .build());
    }

    @PostMapping("/{ticketNumber}/comments")
    public ResponseEntity<ApiResponse<TicketCommentResponse>> addPublicComment(
            @PathVariable String ticketNumber,
            @Valid @RequestBody CreateTicketCommentRequest request
    ) {
        TicketCommentResponse response = ticketService.addPublicComment(ticketNumber, request);
        return ResponseEntity.ok(ApiResponse.<TicketCommentResponse>builder()
                .success(true)
                .message("Reply sent successfully")
                .data(response)
                .build());
    }
}

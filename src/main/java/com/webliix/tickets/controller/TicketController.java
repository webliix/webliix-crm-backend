package com.webliix.tickets.controller;

import com.webliix.shared.response.ApiResponse;
import com.webliix.tickets.dto.*;
import com.webliix.tickets.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('TICKETS_CREATE', 'SUPER_ADMIN', 'ADMIN', 'MANAGER', 'USER', 'ROLE_USER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        TicketResponse response = ticketService.createTicket(request);
        return ResponseEntity.ok(ApiResponse.<TicketResponse>builder()
                .success(true)
                .message("Ticket created successfully")
                .data(response)
                .build());
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TICKETS_VIEW', 'SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE', 'ROLE_EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTickets(@RequestParam(required = false) Long projectId,
                                                                        @RequestParam(required = false) Long customerId) {
        List<TicketResponse> response;
        if (projectId != null) {
            response = ticketService.getTicketsByProject(projectId);
        } else if (customerId != null) {
            response = ticketService.getTicketsByCustomer(customerId);
        } else {
            response = ticketService.getAllTickets();
        }
        return ResponseEntity.ok(ApiResponse.<List<TicketResponse>>builder()
                .success(true)
                .message("Tickets fetched")
                .data(response)
                .build());
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsByProject(@PathVariable Long projectId) {
        List<TicketResponse> response = ticketService.getTicketsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.<List<TicketResponse>>builder()
                .success(true)
                .message("Project tickets fetched")
                .data(response)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicket(@PathVariable Long id) {
        TicketResponse response = ticketService.getTicket(id);
        return ResponseEntity.ok(ApiResponse.<TicketResponse>builder()
                .success(true)
                .message("Ticket fetched")
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TICKETS_EDIT', 'SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE', 'ROLE_EMPLOYEE')")
    public ResponseEntity<ApiResponse<TicketResponse>> updateTicket(@PathVariable Long id,
                                                                    @RequestBody UpdateTicketRequest request) {
        TicketResponse response = ticketService.updateTicket(id, request);
        return ResponseEntity.ok(ApiResponse.<TicketResponse>builder()
                .success(true)
                .message("Ticket updated successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<TicketResponse>> assignTicket(@PathVariable Long id,
                                                                    @Valid @RequestBody AssignTicketRequest request) {
        TicketResponse response = ticketService.assignTicket(id, request);
        return ResponseEntity.ok(ApiResponse.<TicketResponse>builder()
                .success(true)
                .message("Ticket assigned successfully")
                .data(response)
                .build());
    }

    @GetMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TicketCommentResponse>>> getComments(@PathVariable Long id) {
        List<TicketCommentResponse> response = ticketService.getTicketComments(id);
        return ResponseEntity.ok(ApiResponse.<List<TicketCommentResponse>>builder()
                .success(true)
                .message("Comments fetched")
                .data(response)
                .build());
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TicketCommentResponse>> addComment(@PathVariable Long id,
                                                                         @Valid @RequestBody CreateTicketCommentRequest request) {
        TicketCommentResponse response = ticketService.addComment(id, request);
        return ResponseEntity.ok(ApiResponse.<TicketCommentResponse>builder()
                .success(true)
                .message("Comment added successfully")
                .data(response)
                .build());
    }

    @PostMapping("/{id}/attachments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TicketAttachmentResponse>> addAttachment(@PathVariable Long id,
                                                                               @Valid @RequestBody CreateTicketAttachmentRequest request) {
        TicketAttachmentResponse response = ticketService.addAttachment(id, request);
        return ResponseEntity.ok(ApiResponse.<TicketAttachmentResponse>builder()
                .success(true)
                .message("Attachment uploaded successfully")
                .data(response)
                .build());
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE', 'ROLE_EMPLOYEE')")
    public ResponseEntity<ApiResponse<TicketDashboardResponse>> getDashboard() {
        TicketDashboardResponse response = ticketService.getDashboard();
        return ResponseEntity.ok(ApiResponse.<TicketDashboardResponse>builder()
                .success(true)
                .message("Ticket dashboard fetched")
                .data(response)
                .build());
    }
}

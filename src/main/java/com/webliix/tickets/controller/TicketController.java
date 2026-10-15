package com.webliix.tickets.controller;

import com.webliix.security.annotation.RequirePermission;
import com.webliix.security.evaluator.PermissionEvaluator;
import com.webliix.shared.response.ApiResponse;
import com.webliix.tickets.dto.*;
import com.webliix.tickets.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final com.webliix.crm.customer.repository.CustomerRepository customerRepository;
    private final PermissionEvaluator permissionEvaluator;

    @PostMapping
    @RequirePermission(anyOf = {"TICKETS_CREATE", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            org.springframework.security.core.Authentication auth) {
        if ((request.getCreatedBy() == null || request.getCreatedBy().isBlank()) && auth != null) {
            request.setCreatedBy(auth.getName());
        }
        TicketResponse response = ticketService.createTicket(request);
        return ResponseEntity.ok(ApiResponse.<TicketResponse>builder()
                .success(true)
                .message("Ticket created successfully")
                .data(response)
                .build());
    }

    @GetMapping
    @RequirePermission(anyOf = {"TICKETS_VIEW", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTickets(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long customerId,
            org.springframework.security.core.Authentication auth) {
        
        boolean isStaff = permissionEvaluator.has("TICKETS_VIEW");

        List<TicketResponse> response;
        if (!isStaff) {
            String email = auth != null ? auth.getName() : "";
            var customer = customerRepository.findByEmail(email.trim().toLowerCase())
                    .or(() -> customerRepository.findByEmail(email.trim()))
                    .orElse(null);
            if (customer != null) {
                response = ticketService.getTicketsByCustomer(customer.getId());
            } else {
                response = List.of();
            }
        } else if (projectId != null) {
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
    @RequirePermission(anyOf = {"TICKETS_VIEW", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsByProject(@PathVariable Long projectId) {
        List<TicketResponse> response = ticketService.getTicketsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.<List<TicketResponse>>builder()
                .success(true)
                .message("Project tickets fetched")
                .data(response)
                .build());
    }

    @GetMapping("/{id}")
    @RequirePermission(anyOf = {"TICKETS_VIEW", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<TicketResponse>> getTicket(
            @PathVariable Long id,
            org.springframework.security.core.Authentication auth) {
        TicketResponse response = ticketService.getTicket(id);

        boolean isStaff = permissionEvaluator.has("TICKETS_VIEW");

        if (!isStaff) {
            String email = auth != null ? auth.getName() : "";
            var customer = customerRepository.findByEmail(email.trim().toLowerCase())
                    .or(() -> customerRepository.findByEmail(email.trim()))
                    .orElse(null);
            if (customer == null || response.getCustomerId() == null || !response.getCustomerId().equals(customer.getId())) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied: You can only view your own tickets.");
            }
        }

        return ResponseEntity.ok(ApiResponse.<TicketResponse>builder()
                .success(true)
                .message("Ticket fetched")
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    @RequirePermission("TICKETS_EDIT")
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
    @RequirePermission("TICKETS_ASSIGN")
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
    @RequirePermission(anyOf = {"TICKETS_VIEW", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<List<TicketCommentResponse>>> getComments(@PathVariable Long id) {
        List<TicketCommentResponse> response = ticketService.getTicketComments(id);
        return ResponseEntity.ok(ApiResponse.<List<TicketCommentResponse>>builder()
                .success(true)
                .message("Comments fetched")
                .data(response)
                .build());
    }

    @PostMapping("/{id}/comments")
    @RequirePermission(anyOf = {"TICKETS_VIEW", "CLIENT_PORTAL_ACCESS"})
    public ResponseEntity<ApiResponse<TicketCommentResponse>> addComment(@PathVariable Long id,
                                                                         @Valid @RequestBody CreateTicketCommentRequest request,
                                                                         org.springframework.security.core.Authentication auth) {
        if ((request.getCommentedBy() == null || request.getCommentedBy().isBlank()) && auth != null) {
            request.setCommentedBy(auth.getName());
        }
        TicketCommentResponse response = ticketService.addComment(id, request);
        return ResponseEntity.ok(ApiResponse.<TicketCommentResponse>builder()
                .success(true)
                .message("Comment added successfully")
                .data(response)
                .build());
    }

    @PostMapping("/{id}/attachments")
    @RequirePermission(anyOf = {"TICKETS_VIEW", "CLIENT_PORTAL_ACCESS"})
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
    @RequirePermission("TICKETS_VIEW")
    public ResponseEntity<ApiResponse<TicketDashboardResponse>> getDashboard() {
        TicketDashboardResponse response = ticketService.getDashboard();
        return ResponseEntity.ok(ApiResponse.<TicketDashboardResponse>builder()
                .success(true)
                .message("Ticket dashboard fetched")
                .data(response)
                .build());
    }
}

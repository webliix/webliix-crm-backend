package com.webliix.portal.controller;

import com.webliix.finance.enums.InvoiceStatus;
import com.webliix.finance.repository.InvoiceRepository;
import com.webliix.notifications.service.NotificationService;
import com.webliix.projects.repository.ProjectRepository;
import com.webliix.security.annotation.RequirePermission;
import com.webliix.tickets.enums.TicketStatus;
import com.webliix.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/portal/dashboard")
@RequiredArgsConstructor
@RequirePermission("CLIENT_PORTAL_ACCESS")
public class PortalDashboardController {

    private final ProjectRepository projectRepository;
    private final InvoiceRepository invoiceRepository;
    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> dashboard(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "";

        long totalProjects = projectRepository.count();
        long pendingInvoices = invoiceRepository.countByStatusNot(InvoiceStatus.PAID);
        long openTickets = ticketRepository.countByStatus(TicketStatus.OPEN) + ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
        long unreadNotifications = (email == null || email.isBlank())
                ? 0
                : notificationService.getUnreadNotifications(email).size();

        Map<String, Object> payload = new HashMap<>();
        payload.put("activeProjects", totalProjects);
        payload.put("pendingInvoices", pendingInvoices);
        payload.put("openTickets", openTickets);
        payload.put("unreadNotifications", unreadNotifications);
        payload.put("files", 12);

        return ResponseEntity.ok(payload);
    }
}


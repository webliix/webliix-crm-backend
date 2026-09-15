package com.webliix.notifications.listener;

import com.webliix.notifications.dto.CreateNotificationRequest;
import com.webliix.notifications.enums.NotificationChannel;
import com.webliix.notifications.enums.ReferenceType;
import com.webliix.notifications.event.*;
import com.webliix.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void onLeadCreated(LeadCreatedEvent event) {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .title("New Lead Created")
                .message("A new lead '" + event.getLeadName() + "' has been created.")
                .recipient(event.getEmail())
                .recipientType("CUSTOMER")
                .channel(NotificationChannel.EMAIL)
                .referenceType(ReferenceType.LEAD.name())
                .referenceId(event.getLeadId())
                .build();
        notificationService.createNotification(request);
    }

    @EventListener
    public void onTicketAssigned(TicketAssignedEvent event) {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .title("Ticket Assigned")
                .message("Ticket #" + event.getTicketNumber() + " has been assigned to you.")
                .recipient(event.getEmployeeEmail())
                .recipientType("EMPLOYEE")
                .channel(NotificationChannel.EMAIL)
                .referenceType(ReferenceType.TICKET.name())
                .referenceId(event.getTicketId())
                .build();
        notificationService.createNotification(request);
    }

    @EventListener
    public void onInvoicePaid(InvoicePaidEvent event) {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .title("Invoice Paid")
                .message("Invoice #" + event.getInvoiceNumber() + " for amount " + event.getAmount() + " has been paid.")
                .recipient(event.getCustomerEmail())
                .recipientType("CUSTOMER")
                .channel(NotificationChannel.EMAIL)
                .referenceType(ReferenceType.INVOICE.name())
                .referenceId(event.getInvoiceId())
                .build();
        notificationService.createNotification(request);
    }

    @EventListener
    public void onPayrollGenerated(PayrollGeneratedEvent event) {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .title("Payroll Generated")
                .message("Your payroll has been generated for amount " + event.getAmount())
                .recipient(event.getEmployeeEmail())
                .recipientType("EMPLOYEE")
                .channel(NotificationChannel.EMAIL)
                .referenceType(ReferenceType.PAYROLL.name())
                .referenceId(event.getPayrollId())
                .build();
        notificationService.createNotification(request);
    }

    @EventListener
    public void onProjectCreated(ProjectCreatedEvent event) {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .title("New Project Created")
                .message("Project '" + event.getProjectName() + "' has been created for you.")
                .recipient(event.getCustomerEmail())
                .recipientType("CUSTOMER")
                .channel(NotificationChannel.EMAIL)
                .referenceType(ReferenceType.PROJECT.name())
                .referenceId(event.getProjectId())
                .build();
        notificationService.createNotification(request);
    }
}

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
    private final com.webliix.notifications.service.EmailService emailService;

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
                .title("New Project Created: " + event.getProjectName())
                .message("Project '" + event.getProjectName() + "' has been created for you. You can track progress and milestones in your Client Portal.")
                .recipient(event.getCustomerEmail())
                .recipientType("CUSTOMER")
                .channel(NotificationChannel.IN_APP)
                .referenceType(ReferenceType.PROJECT.name())
                .referenceId(event.getProjectId())
                .build();
        notificationService.createNotification(request);

        if (event.getCustomerEmail() != null && !event.getCustomerEmail().isEmpty()) {
            emailService.sendNotificationEmail(
                    event.getCustomerEmail(),
                    "Webliix Project Initiated: " + event.getProjectName(),
                    "Hello, your project '" + event.getProjectName() + "' has been initiated at Webliix. " +
                    "Standard phases and milestones have been configured. You can monitor the live development progress in the Webliix Client Portal."
            );
        }
    }

    @EventListener
    public void onProjectUpdate(ProjectUpdateEvent event) {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .title(event.getTitle() != null ? event.getTitle() : "Project Update: " + event.getProjectName())
                .message(event.getMessage() != null ? event.getMessage() : "Project '" + event.getProjectName() + "' has been updated.")
                .recipient(event.getCustomerEmail())
                .recipientType("CUSTOMER")
                .channel(NotificationChannel.IN_APP)
                .referenceType(ReferenceType.PROJECT.name())
                .referenceId(event.getProjectId())
                .build();
        notificationService.createNotification(request);

        if (event.getCustomerEmail() != null && !event.getCustomerEmail().isEmpty()) {
            String subject = "Project Update [" + event.getProjectName() + "] - " + (event.getStatus() != null ? event.getStatus() : "Progress " + event.getProgressPercentage() + "%");
            String emailBody = "<p>Hello,</p>" +
                    "<p>An update has been posted regarding your project <strong>" + event.getProjectName() + "</strong>:</p>" +
                    "<blockquote>" + (event.getMessage() != null ? event.getMessage() : "Project milestone/status has been updated.") + "</blockquote>" +
                    (event.getProgressPercentage() != null ? "<p><strong>Current Overall Progress:</strong> " + event.getProgressPercentage() + "%</p>" : "") +
                    (event.getStatus() != null ? "<p><strong>Current Status:</strong> " + event.getStatus() + "</p>" : "") +
                    "<p>Log in to your Webliix Client Portal to view full milestone breakdown, architecture specifications, and submit instructions.</p>";
            emailService.sendAutomatedHtmlEmail(event.getCustomerEmail(), subject, emailBody);
        }
    }
}

package com.webliix.crm.lead.listener;

import com.webliix.crm.lead.event.PublicLeadSubmittedEvent;
import com.webliix.notifications.dto.CreateNotificationRequest;
import com.webliix.notifications.enums.NotificationChannel;
import com.webliix.notifications.enums.ReferenceType;
import com.webliix.notifications.service.EmailService;
import com.webliix.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicLeadEventListener {

    private final EmailService emailService;
    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handlePublicLeadSubmitted(PublicLeadSubmittedEvent event) {
        log.info("Processing PublicLeadSubmittedEvent for lead ID: {}, Email: {}", event.getLeadId(), event.getEmail());

        // 1. Dispatch Automated Welcome Inquiry Confirmation Email
        try {
            String emailSubject = "Thank you for contacting Webliix – Project Inquiry Received";
            String emailBody = "Hello " + (event.getContactPerson() != null ? event.getContactPerson() : "Valued Customer") + ",\n\n"
                    + "Thank you for reaching out to Webliix! We have successfully registered your project inquiry.\n\n"
                    + "Inquiry Details:\n"
                    + "• Service Requested: " + (event.getServiceRequested() != null ? event.getServiceRequested() : "General Consultation") + "\n"
                    + "• Company: " + (event.getCompanyName() != null ? event.getCompanyName() : "Individual") + "\n"
                    + "• Summary: \"" + (event.getRequirements() != null ? event.getRequirements() : "N/A") + "\"\n\n"
                    + "Our engineering and solutions team is reviewing your requirements. We will get back to you within 2 business hours.\n\n"
                    + "If you have any urgent attachments or details, feel free to reply directly to this email.\n\n"
                    + "Best regards,\n"
                    + "Webliix Engineering & Solutions Team\n"
                    + "https://webliix.in";

            emailService.sendEmail(event.getEmail(), emailSubject, emailBody);
            log.info("Automated welcome email sent to {}", event.getEmail());
        } catch (Exception ex) {
            log.error("Failed to send automated lead welcome email to {}: {}", event.getEmail(), ex.getMessage());
        }

        // 2. Dispatch Internal CRM Notification for Sales / Admin Team
        try {
            String leadTitle = (event.getServiceRequested() != null ? event.getServiceRequested() : "Public Lead")
                    + " Inquiry - " + event.getContactPerson();

            String notifMessage = "New lead captured from website (" + (event.getServiceRequested() != null ? event.getServiceRequested() : "General") + "). "
                    + "Email: " + event.getEmail() + (event.getPhone() != null ? ", Phone: " + event.getPhone() : "");

            CreateNotificationRequest notifRequest = CreateNotificationRequest.builder()
                    .title(leadTitle)
                    .message(notifMessage)
                    .recipient("ALL")
                    .recipientType("BROADCAST")
                    .channel(NotificationChannel.IN_APP)
                    .referenceType(ReferenceType.LEAD.name())
                    .referenceId(event.getLeadId())
                    .build();

            notificationService.broadcastNotification(notifRequest);
            log.info("Internal CRM notification broadcast created for Lead ID {}", event.getLeadId());
        } catch (Exception ex) {
            log.error("Failed to create internal CRM notification for Lead ID {}: {}", event.getLeadId(), ex.getMessage());
        }
    }
}

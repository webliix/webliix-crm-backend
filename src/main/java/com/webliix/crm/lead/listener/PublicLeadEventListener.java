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

        // 1. Dispatch Automated Welcome Inquiry Confirmation Email (via noreply@webliix.com)
        try {
            String emailSubject = "Thank you for contacting Webliix – Project Inquiry Received";
            String name = event.getContactPerson() != null && !event.getContactPerson().isBlank() ? event.getContactPerson() : "Valued Customer";
            String service = event.getServiceRequested() != null && !event.getServiceRequested().isBlank() ? event.getServiceRequested() : "General Consultation";
            String company = event.getCompanyName() != null && !event.getCompanyName().isBlank() ? event.getCompanyName() : "Individual / Startup";
            String requirements = event.getRequirements() != null && !event.getRequirements().isBlank() ? event.getRequirements() : "N/A";

            java.io.InputStream is = getClass().getResourceAsStream("/templates/emails/lead-inquiry-acknowledgment.html");
            String htmlBody;
            if (is != null) {
                String template = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                htmlBody = template
                        .replace("{{NAME}}", name)
                        .replace("{{SERVICE}}", service)
                        .replace("{{COMPANY}}", company)
                        .replace("{{REQUIREMENTS}}", requirements);
            } else {
                htmlBody = "<p>Hello " + name + ", thank you for contacting Webliix regarding " + service + ". Our team will contact you shortly.</p>";
            }

            emailService.sendAutomatedHtmlEmail(event.getEmail(), emailSubject, htmlBody);
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

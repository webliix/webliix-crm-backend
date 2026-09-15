package com.webliix.automation.action;

import com.webliix.notifications.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("SEND_EMAIL")
public class SendEmailAction implements AutomationAction {

    private static final Logger logger = LoggerFactory.getLogger(SendEmailAction.class);
    private final EmailService emailService;

    public SendEmailAction(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void execute(Map<String, Object> context, Map<String, Object> parameters) {
        String recipient = String.valueOf(parameters.get("recipient"));
        String subject = String.valueOf(parameters.get("subject"));
        String template = String.valueOf(parameters.get("template"));
        String body = String.valueOf(parameters.getOrDefault("body", ""));

        if (recipient == null || recipient.isBlank() || subject == null || subject.isBlank()) {
            logger.warn("SEND_EMAIL action skipped due to missing recipient or subject");
            return;
        }

        if (template != null && !template.isBlank()) {
            emailService.sendTemplateEmail(recipient, template, subject);
        } else if (body != null && !body.isBlank()) {
            emailService.sendHtmlEmail(recipient, subject, body);
        } else {
            emailService.sendEmail(recipient, subject, "Automated notification from Webliix");
        }

        logger.info("Automation SEND_EMAIL executed for recipient={}", recipient);
    }
}

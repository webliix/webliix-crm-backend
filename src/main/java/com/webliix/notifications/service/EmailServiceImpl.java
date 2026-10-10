package com.webliix.notifications.service;

import com.webliix.notifications.strategy.MailSenderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final MailSenderStrategy mailSenderStrategy;

    @Value("${mail.automated.from:${mail.contact.from:contact@webliix.com}}")
    private String automatedFromEmail;

    @Value("${mail.automated.from-name:Webliix Team}")
    private String automatedFromName;

    @Value("${mail.contact.from:contact@webliix.com}")
    private String contactFromEmail;

    @Value("${mail.contact.from-name:Webliix Team}")
    private String contactFromName;

    public EmailServiceImpl(MailSenderStrategy mailSenderStrategy) {
        this.mailSenderStrategy = mailSenderStrategy;
    }

    @Async
    @Override
    public void sendEmail(String to, String subject, String body) {
        sendConversationalEmail(to, subject, body);
    }

    @Async
    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        sendConversationalHtmlEmail(to, subject, htmlBody);
    }

    @Async
    @Override
    public void sendAutomatedHtmlEmail(String to, String subject, String htmlBody) {
        try {
            boolean success = mailSenderStrategy.sendMail(
                    automatedFromEmail,
                    automatedFromName,
                    to,
                    subject,
                    htmlBody,
                    true
            );
            if (success) {
                log.info("Automated HTML email sent to {} via {}", to, mailSenderStrategy.getProviderName());
            } else {
                log.error("Failed to send automated HTML email to {}", to);
            }
        } catch (Exception e) {
            log.error("Exception during automated email dispatch to {}: {}", to, e.getMessage());
        }
    }

    @Async
    @Override
    public void sendConversationalEmail(String to, String subject, String body) {
        try {
            boolean success = mailSenderStrategy.sendMail(
                    contactFromEmail,
                    contactFromName,
                    to,
                    subject,
                    body,
                    false
            );
            if (success) {
                log.info("Conversational text email sent to {} via {}", to, mailSenderStrategy.getProviderName());
            } else {
                log.error("Failed to send conversational text email to {}", to);
            }
        } catch (Exception e) {
            log.error("Exception during conversational text email dispatch to {}: {}", to, e.getMessage());
        }
    }

    private void sendConversationalHtmlEmail(String to, String subject, String htmlBody) {
        try {
            boolean success = mailSenderStrategy.sendMail(
                    contactFromEmail,
                    contactFromName,
                    to,
                    subject,
                    htmlBody,
                    true
            );
            if (success) {
                log.info("Conversational HTML email sent to {} via {}", to, mailSenderStrategy.getProviderName());
            } else {
                log.error("Failed to send conversational HTML email to {}", to);
            }
        } catch (Exception e) {
            log.error("Exception during conversational HTML email dispatch to {}: {}", to, e.getMessage());
        }
    }

    @Async
    @Override
    public void sendTemplateEmail(String to, String templateName, String subject) {
        String htmlBody = loadTemplate(templateName);
        if (htmlBody != null) {
            sendAutomatedHtmlEmail(to, subject, htmlBody);
        } else {
            log.error("Could not load template: {}", templateName);
        }
    }

    @Async
    @Override
    public void sendNotificationEmail(String to, String title, String message) {
        String template = loadTemplate("system-notification");
        String htmlBody;
        if (template != null) {
            htmlBody = template
                    .replace("{{TITLE}}", title != null ? title : "Webliix Notification")
                    .replace("{{MESSAGE}}", message != null ? message : "");
        } else {
            htmlBody = buildNotificationHtmlFallback(title, message);
        }
        sendAutomatedHtmlEmail(to, title, htmlBody);
    }

    @Async
    @Override
    public void sendVerificationOtpEmail(String to, String name, String otp, int expirationMinutes) {
        try {
            mailSenderStrategy.unblockRecipient(to);
        } catch (Exception ex) {
            log.warn("Notice: unblockRecipient for {} had issue: {}", to, ex.getMessage());
        }
        String template = loadTemplate("email-verification-otp");
        if (template != null) {
            String html = template
                    .replace("{{NAME}}", name != null ? name : "User")
                    .replace("{{OTP}}", otp)
                    .replace("{{EXPIRATION_MINUTES}}", String.valueOf(expirationMinutes));
            sendAutomatedHtmlEmail(to, "Webliix Hub - Email Verification Code", html);
        } else {
            sendAutomatedHtmlEmail(to, "Webliix Hub - Email Verification Code",
                    "<p>Hello " + name + ", your verification code is: <strong>" + otp + "</strong>. Valid for " + expirationMinutes + " minutes.</p>");
        }
    }

    @Async
    @Override
    public void sendForgotPasswordOtpEmail(String to, String name, String otp, int expirationMinutes) {
        try {
            mailSenderStrategy.unblockRecipient(to);
        } catch (Exception ex) {
            log.warn("Notice: unblockRecipient for {} had issue: {}", to, ex.getMessage());
        }
        String template = loadTemplate("forgot-password-otp");
        if (template != null) {
            String html = template
                    .replace("{{NAME}}", name != null ? name : "User")
                    .replace("{{OTP}}", otp)
                    .replace("{{EXPIRATION_MINUTES}}", String.valueOf(expirationMinutes));
            sendAutomatedHtmlEmail(to, "Webliix Hub - Password Reset Code", html);
        } else {
            sendAutomatedHtmlEmail(to, "Webliix Hub - Password Reset Code",
                    "<p>Hello " + name + ", your password reset code is: <strong>" + otp + "</strong>. Valid for " + expirationMinutes + " minutes.</p>");
        }
    }

    @Async
    @Override
    public void sendPasswordResetSuccessEmail(String to, String name) {
        String template = loadTemplate("password-reset-success");
        if (template != null) {
            String html = template.replace("{{NAME}}", name != null ? name : "User");
            sendAutomatedHtmlEmail(to, "Webliix Hub - Password Reset Successful", html);
        } else {
            sendAutomatedHtmlEmail(to, "Webliix Hub - Password Reset Successful",
                    "<p>Hello " + name + ", your Webliix account password has been successfully reset.</p>");
        }
    }

    private String loadTemplate(String templateName) {
        try {
            InputStream is = getClass().getResourceAsStream("/templates/emails/" + templateName + ".html");
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("Failed to load email template: {}", templateName, e);
        }
        return null;
    }

    private String buildNotificationHtmlFallback(String title, String message) {
        return "<html><body style='font-family: Arial, sans-serif;'>" +
                "<div style='background-color: #f5f5f5; padding: 20px; border-radius: 5px;'>" +
                "<h2 style='color: #333;'>" + title + "</h2>" +
                "<p style='color: #666; line-height: 1.6;'>" + message + "</p>" +
                "<p style='margin-top: 20px; color: #999; font-size: 12px;'>This is an automated message from noreply@webliix.com</p>" +
                "</div></body></html>";
    }
}

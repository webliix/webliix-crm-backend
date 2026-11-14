package com.webliix.notifications.service;

import com.webliix.notifications.strategy.MailSenderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final MailSenderStrategy mailSenderStrategy;
    private final com.webliix.notifications.repository.EmailTemplateRepository emailTemplateRepository;
    private final com.webliix.settings.service.SettingsCacheService settingsCacheService;

    @Value("${mail.automated.from:noreply@webliix.com}")
    private String automatedFromEmail;

    @Value("${mail.automated.from-name:Webliix Team}")
    private String automatedFromName;

    @Value("${mail.contact.from:contact@webliix.com}")
    private String contactFromEmail;

    @Value("${mail.contact.from-name:Webliix Team}")
    private String contactFromName;

    @Value("${mail.support.from:support@webliix.com}")
    private String supportFromEmail;

    @Value("${mail.support.from-name:Webliix Support}")
    private String supportFromName;

    public EmailServiceImpl(
            MailSenderStrategy mailSenderStrategy,
            com.webliix.notifications.repository.EmailTemplateRepository emailTemplateRepository,
            com.webliix.settings.service.SettingsCacheService settingsCacheService) {
        this.mailSenderStrategy = mailSenderStrategy;
        this.emailTemplateRepository = emailTemplateRepository;
        this.settingsCacheService = settingsCacheService;
    }

    @Async
    @Override
    public void sendSupportHtmlEmail(String to, String subject, String htmlBody) {
        try {
            boolean success = mailSenderStrategy.sendMail(
                    supportFromEmail,
                    supportFromName,
                    to,
                    subject,
                    htmlBody,
                    true
            );
            if (success) {
                log.info("Support HTML email sent to {} via {}", to, mailSenderStrategy.getProviderName());
            } else {
                log.error("Failed to send support HTML email to {}", to);
            }
        } catch (Exception e) {
            log.error("Exception during support HTML email dispatch to {}: {}", to, e.getMessage());
        }
    }

    @Async
    @Override
    public void sendCustomHtmlEmail(String to, String subject, String htmlBody) {
        sendConversationalHtmlEmail(to, subject, htmlBody);
    }

    @Async
    @Override
    public void sendTemplateEmailWithSender(String to, String templateName, String subject, java.util.Map<String, String> variables, String senderEmail, String senderName) {
        String htmlBody = loadTemplate(templateName);
        if (htmlBody != null && variables != null) {
            for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
                String val = entry.getValue() != null ? entry.getValue() : "";
                htmlBody = htmlBody.replace("{{" + entry.getKey() + "}}", val)
                                   .replace("{{" + entry.getKey().toUpperCase() + "}}", val)
                                   .replace("{{" + entry.getKey().toLowerCase() + "}}", val);
            }
        }
        String chosenFrom = (senderEmail != null && !senderEmail.isBlank()) ? senderEmail : contactFromEmail;
        String chosenName = (senderName != null && !senderName.isBlank()) ? senderName : contactFromName;
        try {
            mailSenderStrategy.sendMail(chosenFrom, chosenName, to, subject, htmlBody != null ? htmlBody : subject, true);
        } catch (Exception e) {
            log.error("Failed to send custom template email to {}: {}", to, e.getMessage());
        }
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
            com.webliix.settings.entity.SystemSetting globalSetting = settingsCacheService.getByKey("notify.global.email_enabled");
            if (globalSetting != null && "false".equalsIgnoreCase(globalSetting.getSettingValue())) {
                log.info("Global email dispatch disabled in system settings. Suppressed email to: {}", to);
                return;
            }

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
            // First check customized template in DB
            Optional<com.webliix.notifications.entity.EmailTemplate> dbTemplate = emailTemplateRepository.findByTemplateKey(templateName);
            String htmlContent = null;
            String customHeader = null;
            String customFooter = null;

            if (dbTemplate.isPresent() && Boolean.TRUE.equals(dbTemplate.get().getIsActive()) && dbTemplate.get().getHtmlContent() != null) {
                htmlContent = dbTemplate.get().getHtmlContent();
                customHeader = dbTemplate.get().getHeaderHtml();
                customFooter = dbTemplate.get().getFooterHtml();
            } else {
                // Fallback to classpath template
                InputStream is = getClass().getResourceAsStream("/templates/emails/" + templateName + ".html");
                if (is != null) {
                    htmlContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            }

            if (htmlContent == null) return null;

            // Resolve Global Layout from Settings cache
            String globalHeader = null;
            String globalFooter = null;
            try {
                com.webliix.settings.entity.SystemSetting h = settingsCacheService.getByKey("email.global.header_html");
                if (h != null && h.getSettingValue() != null && !h.getSettingValue().isBlank()) {
                    globalHeader = h.getSettingValue();
                } else {
                    globalHeader = EmailTemplateServiceImpl.DEFAULT_GLOBAL_HEADER;
                }
                com.webliix.settings.entity.SystemSetting f = settingsCacheService.getByKey("email.global.footer_html");
                if (f != null && f.getSettingValue() != null && !f.getSettingValue().isBlank()) {
                    globalFooter = f.getSettingValue();
                } else {
                    globalFooter = EmailTemplateServiceImpl.DEFAULT_GLOBAL_FOOTER;
                }
            } catch (Exception ex) {
                globalHeader = EmailTemplateServiceImpl.DEFAULT_GLOBAL_HEADER;
                globalFooter = EmailTemplateServiceImpl.DEFAULT_GLOBAL_FOOTER;
            }

            String activeHeader = (customHeader != null && !customHeader.isBlank()) ? customHeader : globalHeader;
            String activeFooter = (customFooter != null && !customFooter.isBlank()) ? customFooter : globalFooter;

            return EmailTemplateServiceImpl.applyLayout(htmlContent, activeHeader, activeFooter);
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

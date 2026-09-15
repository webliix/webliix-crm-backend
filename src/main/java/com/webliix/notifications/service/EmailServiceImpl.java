package com.webliix.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@webliix.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("noreply@webliix.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
        }
    }

    @Override
    public void sendTemplateEmail(String to, String templateName, String subject) {
        String htmlBody = loadTemplate(templateName);
        sendHtmlEmail(to, subject, htmlBody);
    }

    @Override
    public void sendNotificationEmail(String to, String title, String message) {
        String htmlBody = buildNotificationHtml(title, message);
        sendHtmlEmail(to, title, htmlBody);
    }

    private String loadTemplate(String templateName) {
        try {
            return new String(java.nio.file.Files.readAllBytes(
                    java.nio.file.Paths.get("src/main/resources/templates/emails/" + templateName + ".html")));
        } catch (Exception e) {
            log.error("Failed to load email template: {}", templateName);
            return "<p>" + templateName + "</p>";
        }
    }

    private String buildNotificationHtml(String title, String message) {
        return "<html><body style='font-family: Arial, sans-serif;'>" +
                "<div style='background-color: #f5f5f5; padding: 20px; border-radius: 5px;'>" +
                "<h2 style='color: #333;'>" + title + "</h2>" +
                "<p style='color: #666; line-height: 1.6;'>" + message + "</p>" +
                "<p style='margin-top: 20px; color: #999; font-size: 12px;'>This is an automated message from Webliix</p>" +
                "</div></body></html>";
    }
}

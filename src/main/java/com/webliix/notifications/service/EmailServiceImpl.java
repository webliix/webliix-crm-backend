package com.webliix.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${MAIL_FROM:contact@webliix.com}")
    private String mailFrom;

    @Value("${MAIL_FROM_NAME:Webliix Hub}")
    private String mailFromName;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
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
            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
        }
    }

    @Override
    public void sendTemplateEmail(String to, String templateName, String subject) {
        String htmlBody = loadTemplate(templateName);
        if (htmlBody != null) {
            sendHtmlEmail(to, subject, htmlBody);
        }
    }

    @Override
    public void sendNotificationEmail(String to, String title, String message) {
        String htmlBody = buildNotificationHtml(title, message);
        sendHtmlEmail(to, title, htmlBody);
    }

    @Override
    public void sendVerificationOtpEmail(String to, String name, String otp, int expirationMinutes) {
        String template = loadTemplate("email-verification-otp");
        if (template != null) {
            String html = template
                    .replace("{{NAME}}", name != null ? name : "User")
                    .replace("{{OTP}}", otp)
                    .replace("{{EXPIRATION_MINUTES}}", String.valueOf(expirationMinutes));
            sendHtmlEmail(to, "Webliix Hub - Email Verification Code", html);
        } else {
            sendEmail(to, "Webliix Hub - Email Verification Code",
                    "Your verification code is: " + otp + ". Valid for " + expirationMinutes + " minutes.");
        }
    }

    @Override
    public void sendForgotPasswordOtpEmail(String to, String name, String otp, int expirationMinutes) {
        String template = loadTemplate("forgot-password-otp");
        if (template != null) {
            String html = template
                    .replace("{{NAME}}", name != null ? name : "User")
                    .replace("{{OTP}}", otp)
                    .replace("{{EXPIRATION_MINUTES}}", String.valueOf(expirationMinutes));
            sendHtmlEmail(to, "Webliix Hub - Password Reset Code", html);
        } else {
            sendEmail(to, "Webliix Hub - Password Reset Code",
                    "Your password reset code is: " + otp + ". Valid for " + expirationMinutes + " minutes.");
        }
    }

    @Override
    public void sendPasswordResetSuccessEmail(String to, String name) {
        String template = loadTemplate("password-reset-success");
        if (template != null) {
            String html = template.replace("{{NAME}}", name != null ? name : "User");
            sendHtmlEmail(to, "Webliix Hub - Password Reset Successful", html);
        } else {
            sendEmail(to, "Webliix Hub - Password Reset Successful",
                    "Hello " + name + ", your password has been successfully reset.");
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

    private String buildNotificationHtml(String title, String message) {
        return "<html><body style='font-family: Arial, sans-serif;'>" +
                "<div style='background-color: #f5f5f5; padding: 20px; border-radius: 5px;'>" +
                "<h2 style='color: #333;'>" + title + "</h2>" +
                "<p style='color: #666; line-height: 1.6;'>" + message + "</p>" +
                "<p style='margin-top: 20px; color: #999; font-size: 12px;'>This is an automated message from Webliix</p>" +
                "</div></body></html>";
    }
}

package com.webliix.notifications.service;

public interface EmailService {

    void sendEmail(String to, String subject, String body);

    void sendHtmlEmail(String to, String subject, String htmlBody);

    void sendAutomatedHtmlEmail(String to, String subject, String htmlBody);

    void sendConversationalEmail(String to, String subject, String body);

    void sendTemplateEmail(String to, String templateName, String subject);

    void sendNotificationEmail(String to, String title, String message);

    void sendVerificationOtpEmail(String to, String name, String otp, int expirationMinutes);

    void sendForgotPasswordOtpEmail(String to, String name, String otp, int expirationMinutes);

    void sendPasswordResetSuccessEmail(String to, String name);

    void sendSupportHtmlEmail(String to, String subject, String htmlBody);

    void sendCustomHtmlEmail(String to, String subject, String htmlBody);

    void sendTemplateEmailWithSender(String to, String templateName, String subject, java.util.Map<String, String> variables, String senderEmail, String senderName);
}

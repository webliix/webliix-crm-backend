package com.webliix.notifications.service;

public interface EmailService {

    void sendEmail(String to, String subject, String body);

    void sendHtmlEmail(String to, String subject, String htmlBody);

    void sendTemplateEmail(String to, String templateName, String subject);

    void sendNotificationEmail(String to, String title, String message);
}

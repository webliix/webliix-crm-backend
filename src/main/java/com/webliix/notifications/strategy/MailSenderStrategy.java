package com.webliix.notifications.strategy;

/**
 * Strategy interface for sending emails across pluggable providers
 * (e.g. Brevo REST API, Spring Mail SMTP).
 */
public interface MailSenderStrategy {

    /**
     * Send email to recipient
     * @param fromEmail Sender email address
     * @param fromName Sender display name
     * @param toEmail Recipient email address
     * @param subject Email subject
     * @param content Body content (HTML or plain text)
     * @param isHtml True if content is HTML
     * @return True if sent successfully
     */
    boolean sendMail(String fromEmail, String fromName, String toEmail, String subject, String content, boolean isHtml);

    /**
     * Check if this strategy is active and configured
     */
    boolean isAvailable();

    /**
     * Unblock recipient if supported by provider
     */
    default boolean unblockRecipient(String email) {
        return false;
    }

    /**
     * Strategy provider name for logging
     */
    String getProviderName();
}

package com.webliix.notifications.strategy.impl;

import com.webliix.notifications.strategy.MailSenderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Primary
public class CompositeMailSenderStrategy implements MailSenderStrategy {

    private final BrevoApiMailSenderStrategy brevoApiStrategy;
    private final SmtpMailSenderStrategy smtpStrategy;

    public CompositeMailSenderStrategy(BrevoApiMailSenderStrategy brevoApiStrategy, SmtpMailSenderStrategy smtpStrategy) {
        this.brevoApiStrategy = brevoApiStrategy;
        this.smtpStrategy = smtpStrategy;
    }

    @Override
    public boolean sendMail(String fromEmail, String fromName, String toEmail, String subject, String content, boolean isHtml) {
        if (brevoApiStrategy.isAvailable()) {
            log.info("Attempting email delivery via Brevo REST API Strategy to {}", toEmail);
            boolean success = brevoApiStrategy.sendMail(fromEmail, fromName, toEmail, subject, content, isHtml);
            if (success) {
                return true;
            }
            log.warn("Brevo REST API strategy failed or was rejected. Attempting fallback via SMTP Strategy.");
        }

        if (smtpStrategy.isAvailable()) {
            return smtpStrategy.sendMail(fromEmail, fromName, toEmail, subject, content, isHtml);
        }

        log.error("All email sender strategies failed or are unconfigured for recipient: {}", toEmail);
        return false;
    }

    @Override
    public boolean isAvailable() {
        return brevoApiStrategy.isAvailable() || smtpStrategy.isAvailable();
    }

    @Override
    public String getProviderName() {
        return "Composite Mail Sender (Brevo API -> SMTP Fallback)";
    }
}

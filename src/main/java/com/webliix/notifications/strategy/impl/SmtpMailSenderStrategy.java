package com.webliix.notifications.strategy.impl;

import com.webliix.notifications.strategy.MailSenderStrategy;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmtpMailSenderStrategy implements MailSenderStrategy {

    private final JavaMailSender mailSender;

    @Override
    public boolean sendMail(String fromEmail, String fromName, String toEmail, String subject, String content, boolean isHtml) {
        try {
            if (isHtml) {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                if (fromName != null && !fromName.isBlank()) {
                    helper.setFrom(fromEmail, fromName);
                } else {
                    helper.setFrom(fromEmail);
                }
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(content, true);
                mailSender.send(message);
            } else {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(content);
                mailSender.send(message);
            }
            log.info("Successfully sent email via SMTP to: {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("SMTP Mail Sender strategy failed to send to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isAvailable() {
        return mailSender != null;
    }

    @Override
    public String getProviderName() {
        return "SMTP Mail Sender";
    }
}

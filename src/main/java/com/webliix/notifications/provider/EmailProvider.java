package com.webliix.notifications.provider;

import com.webliix.notifications.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailProvider implements NotificationProvider {

    private final EmailService emailService;

    @Override
    public void send(String recipient, String title, String message) {
        emailService.sendNotificationEmail(recipient, title, message);
    }

    @Override
    public String getChannelName() {
        return "EMAIL";
    }

    @Override
    public boolean isActive() {
        return true;
    }
}

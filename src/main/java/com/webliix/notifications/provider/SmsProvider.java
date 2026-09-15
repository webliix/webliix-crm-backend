package com.webliix.notifications.provider;

import org.springframework.stereotype.Component;

@Component
public class SmsProvider implements NotificationProvider {

    @Override
    public void send(String recipient, String title, String message) {
        // TODO: Implement SMS provider (e.g., Twilio integration)
    }

    @Override
    public String getChannelName() {
        return "SMS";
    }

    @Override
    public boolean isActive() {
        return false;
    }
}

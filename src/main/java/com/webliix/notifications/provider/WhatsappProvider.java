package com.webliix.notifications.provider;

import org.springframework.stereotype.Component;

@Component
public class WhatsappProvider implements NotificationProvider {

    @Override
    public void send(String recipient, String title, String message) {
        // TODO: Implement WhatsApp provider (Meta Cloud API)
    }

    @Override
    public String getChannelName() {
        return "WHATSAPP";
    }

    @Override
    public boolean isActive() {
        return false;
    }
}

package com.webliix.notifications.provider;

public interface NotificationProvider {

    void send(String recipient, String title, String message);

    String getChannelName();

    boolean isActive();
}

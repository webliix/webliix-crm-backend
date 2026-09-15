package com.webliix.mobile.notification;

import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class PushNotificationService {

    // Placeholder implementation. Integrate Firebase Admin SDK for production.
    public boolean sendToDevice(String fcmToken, String title, String body, Map<String, String> data) {
        // TODO: implement FCM send logic
        return false;
    }
}

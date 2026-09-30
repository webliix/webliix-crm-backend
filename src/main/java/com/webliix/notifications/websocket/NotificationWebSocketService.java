package com.webliix.notifications.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotificationToUser(String userId, String title, String message) {
        messagingTemplate.convertAndSend("/topic/notifications/" + userId,
                new WebSocketNotification(title, message));
    }

    public void broadcastNotification(String title, String message) {
        messagingTemplate.convertAndSend("/topic/notifications",
                new WebSocketNotification(title, message));
    }

    public void broadcastTicketUpdate(Long ticketId, String title, String message, Object payload) {
        messagingTemplate.convertAndSend("/topic/tickets",
                new WebSocketTicketMessage(ticketId, title, message, payload));
        messagingTemplate.convertAndSend("/topic/notifications",
                new WebSocketNotification(title, message));
    }

    public record WebSocketNotification(String title, String message) {}
    public record WebSocketTicketMessage(Long ticketId, String title, String message, Object payload) {}
}

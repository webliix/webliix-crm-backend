package com.webliix.notifications.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotificationToUser(String userId, String title, String message) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications/" + userId,
                    new WebSocketNotification(title, message));
        } catch (Exception e) {
            log.warn("Failed to send WebSocket notification to user {}: {}", userId, e.getMessage());
        }
    }

    public void broadcastNotification(String title, String message) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications",
                    new WebSocketNotification(title, message));
        } catch (Exception e) {
            log.warn("Failed to broadcast WebSocket notification: {}", e.getMessage());
        }
    }

    public void broadcastTicketUpdate(Long ticketId, String title, String message, Object payload) {
        try {
            messagingTemplate.convertAndSend("/topic/tickets",
                    new WebSocketTicketMessage(ticketId, title, message, payload));
            messagingTemplate.convertAndSend("/topic/notifications",
                    new WebSocketNotification(title, message));
        } catch (Exception e) {
            log.warn("Failed to broadcast WebSocket ticket update for ticket {}: {}", ticketId, e.getMessage());
        }
    }

    public record WebSocketNotification(String title, String message) {}
    public record WebSocketTicketMessage(Long ticketId, String title, String message, Object payload) {}
}

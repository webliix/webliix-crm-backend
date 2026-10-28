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
            WebSocketNotification notif = new WebSocketNotification(title, message);
            messagingTemplate.convertAndSend("/topic/notifications/" + userId, notif);
            messagingTemplate.convertAndSend("/topic/notifications", notif);
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
            WebSocketTicketMessage ticketMsg = new WebSocketTicketMessage(ticketId, title, message, payload);
            messagingTemplate.convertAndSend("/topic/tickets", ticketMsg);
            if (ticketId != null) {
                messagingTemplate.convertAndSend("/topic/tickets/" + ticketId, ticketMsg);
            }
            messagingTemplate.convertAndSend("/topic/notifications",
                    new WebSocketNotification(title, message));
        } catch (Exception e) {
            log.warn("Failed to broadcast WebSocket ticket update for ticket {}: {}", ticketId, e.getMessage());
        }
    }

    public void sendTicketAssignedToEmployee(Long employeeId, String email, Long ticketId, String ticketNumber, String subject, Object payload) {
        try {
            String title = "Ticket Assigned: " + ticketNumber;
            String message = subject != null ? subject : "A support ticket has been assigned to you.";
            WebSocketTicketMessage ticketMsg = new WebSocketTicketMessage(ticketId, title, message, payload);

            if (employeeId != null) {
                messagingTemplate.convertAndSend("/topic/employee/" + employeeId + "/tickets", ticketMsg);
                messagingTemplate.convertAndSend("/topic/notifications/" + employeeId, new WebSocketNotification(title, message));
            }
            if (email != null && !email.isBlank()) {
                String cleanEmail = email.trim().toLowerCase();
                messagingTemplate.convertAndSend("/topic/employee/" + cleanEmail + "/tickets", ticketMsg);
                messagingTemplate.convertAndSend("/topic/notifications/" + cleanEmail, new WebSocketNotification(title, message));
            }
            messagingTemplate.convertAndSend("/topic/tickets", ticketMsg);
            if (ticketId != null) {
                messagingTemplate.convertAndSend("/topic/tickets/" + ticketId, ticketMsg);
            }
            messagingTemplate.convertAndSend("/topic/notifications", new WebSocketNotification(title, message));
            log.info("Dispatched WebSocket assignment message for ticket {} to employee {}", ticketNumber, employeeId);
        } catch (Exception e) {
            log.warn("Failed to dispatch ticket assigned WebSocket update to employee {}: {}", employeeId, e.getMessage());
        }
    }

    public void sendTicketCommentToEmployee(Long employeeId, String email, Long ticketId, String ticketNumber, String commentText, Object payload) {
        try {
            String title = "New Message on Ticket " + ticketNumber;
            WebSocketTicketMessage ticketMsg = new WebSocketTicketMessage(ticketId, title, commentText, payload);

            if (employeeId != null) {
                messagingTemplate.convertAndSend("/topic/employee/" + employeeId + "/tickets", ticketMsg);
                messagingTemplate.convertAndSend("/topic/notifications/" + employeeId, new WebSocketNotification(title, commentText));
            }
            if (email != null && !email.isBlank()) {
                String cleanEmail = email.trim().toLowerCase();
                messagingTemplate.convertAndSend("/topic/employee/" + cleanEmail + "/tickets", ticketMsg);
                messagingTemplate.convertAndSend("/topic/notifications/" + cleanEmail, new WebSocketNotification(title, commentText));
            }
            messagingTemplate.convertAndSend("/topic/tickets", ticketMsg);
            if (ticketId != null) {
                messagingTemplate.convertAndSend("/topic/tickets/" + ticketId, ticketMsg);
            }
        } catch (Exception e) {
            log.warn("Failed to dispatch ticket comment WebSocket update to employee {}: {}", employeeId, e.getMessage());
        }
    }

    public record WebSocketNotification(String title, String message) {}
    public record WebSocketTicketMessage(Long ticketId, String title, String message, Object payload) {}
}

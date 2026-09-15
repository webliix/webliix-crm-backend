package com.webliix.automation.action;

import com.webliix.notifications.dto.CreateNotificationRequest;
import com.webliix.notifications.enums.NotificationChannel;
import com.webliix.notifications.enums.NotificationStatus;
import com.webliix.notifications.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("CREATE_NOTIFICATION")
public class CreateNotificationAction implements AutomationAction {

    private static final Logger logger = LoggerFactory.getLogger(CreateNotificationAction.class);
    private final NotificationService notificationService;

    public CreateNotificationAction(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void execute(Map<String, Object> context, Map<String, Object> parameters) {
        String title = String.valueOf(parameters.getOrDefault("title", "Automated notification"));
        String message = String.valueOf(parameters.getOrDefault("message", "A rule executed successfully."));
        String recipient = String.valueOf(parameters.getOrDefault("recipient", "system"));

        Long referenceId = parseReferenceId(parameters.get("referenceId"));

        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .title(title)
                .message(message)
                .recipient(recipient)
                .recipientType(String.valueOf(parameters.getOrDefault("recipientType", "USER")))
                .channel(NotificationChannel.IN_APP)
                .status(NotificationStatus.PENDING)
                .referenceType(String.valueOf(parameters.getOrDefault("referenceType", "AUTOMATION")))
                .referenceId(referenceId)
                .build();

        notificationService.createNotification(request);
        logger.info("Automation CREATE_NOTIFICATION executed: {}", title);
    }

    private Long parseReferenceId(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String) {
            String text = ((String) value).trim();
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}

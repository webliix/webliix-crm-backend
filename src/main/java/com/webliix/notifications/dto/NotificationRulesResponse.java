package com.webliix.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRulesResponse {

    private boolean globalEmailEnabled;
    private boolean globalInAppEnabled;
    private List<NotificationRuleItem> rules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationRuleItem {
        private String key;
        private String name;
        private String category;
        private String description;
        private boolean emailEnabled;
        private boolean inAppEnabled;
        private String templateKey;
    }
}

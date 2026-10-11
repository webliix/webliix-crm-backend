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
public class UpdateNotificationRulesRequest {

    private Boolean globalEmailEnabled;
    private Boolean globalInAppEnabled;
    private List<RuleToggle> rules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleToggle {
        private String key;
        private Boolean emailEnabled;
        private Boolean inAppEnabled;
    }
}

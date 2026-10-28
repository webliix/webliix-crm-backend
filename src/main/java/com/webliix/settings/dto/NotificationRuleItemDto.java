package com.webliix.settings.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRuleItemDto {
    private String key;
    private String name;
    private String category;
    private String description;
    private boolean emailEnabled;
    private boolean inAppEnabled;
}

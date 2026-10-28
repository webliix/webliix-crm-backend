package com.webliix.settings.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRulesConfigDto {
    @Builder.Default
    private boolean globalEmailEnabled = true;

    @Builder.Default
    private boolean globalInAppEnabled = true;

    private List<NotificationRuleItemDto> rules;
}

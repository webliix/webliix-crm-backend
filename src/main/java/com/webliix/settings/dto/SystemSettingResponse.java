package com.webliix.settings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingResponse {
    private Long id;
    private String settingKey;
    private String settingValue;
    private String settingGroup;
    private String description;
    private String dataType;
    private Boolean editable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

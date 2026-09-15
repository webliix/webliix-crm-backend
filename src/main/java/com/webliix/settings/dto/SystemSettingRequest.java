package com.webliix.settings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingRequest {
    private String settingKey;
    private String settingValue;
    private String settingGroup;
    private String description;
    private String dataType;
    private Boolean editable;
}

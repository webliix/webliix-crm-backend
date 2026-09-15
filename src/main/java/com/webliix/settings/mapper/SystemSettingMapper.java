package com.webliix.settings.mapper;

import com.webliix.settings.dto.SystemSettingRequest;
import com.webliix.settings.dto.SystemSettingResponse;
import com.webliix.settings.entity.SystemSetting;

public class SystemSettingMapper {

    public static SystemSettingResponse toResponse(SystemSetting entity) {
        if (entity == null) {
            return null;
        }
        return SystemSettingResponse.builder()
                .id(entity.getId())
                .settingKey(entity.getSettingKey())
                .settingValue(entity.getSettingValue())
                .settingGroup(entity.getSettingGroup())
                .description(entity.getDescription())
                .dataType(entity.getDataType())
                .editable(entity.getEditable())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static SystemSetting toEntity(SystemSettingRequest request) {
        if (request == null) {
            return null;
        }
        return SystemSetting.builder()
                .settingKey(request.getSettingKey())
                .settingValue(request.getSettingValue())
                .settingGroup(request.getSettingGroup())
                .description(request.getDescription())
                .dataType(request.getDataType())
                .editable(request.getEditable())
                .build();
    }
}

package com.webliix.settings.service;

import com.webliix.settings.dto.SystemSettingRequest;
import com.webliix.settings.dto.SystemSettingResponse;
import com.webliix.settings.entity.SystemSetting;
import com.webliix.settings.mapper.SystemSettingMapper;
import com.webliix.settings.repository.SystemSettingRepository;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;
    private final SettingsCacheService settingsCacheService;

    @Transactional
    public SystemSettingResponse createSetting(SystemSettingRequest request) {
        if (request.getSettingKey() == null || request.getSettingKey().isBlank()) {
            throw new IllegalArgumentException("settingKey is required");
        }
        if (systemSettingRepository.existsBySettingKey(request.getSettingKey())) {
            throw new IllegalArgumentException("Setting key already exists: " + request.getSettingKey());
        }

        SystemSetting setting = SystemSettingMapper.toEntity(request);
        setting.setEditable(Boolean.TRUE.equals(request.getEditable()));
        setting.setCreatedAt(LocalDateTime.now());
        setting.setUpdatedAt(LocalDateTime.now());
        SystemSetting saved = systemSettingRepository.save(setting);
        settingsCacheService.reloadCache();
        return SystemSettingMapper.toResponse(saved);
    }

    @Transactional
    public SystemSettingResponse updateSetting(Long id, SystemSettingRequest request) {
        SystemSetting existing = systemSettingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SystemSetting not found with id: " + id));

        if (request.getSettingKey() != null && !request.getSettingKey().equals(existing.getSettingKey())) {
            if (systemSettingRepository.existsBySettingKey(request.getSettingKey())) {
                throw new IllegalArgumentException("Setting key already exists: " + request.getSettingKey());
            }
            existing.setSettingKey(request.getSettingKey());
        }
        if (request.getSettingValue() != null) {
            existing.setSettingValue(request.getSettingValue());
        }
        if (request.getSettingGroup() != null) {
            existing.setSettingGroup(request.getSettingGroup());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getDataType() != null) {
            existing.setDataType(request.getDataType());
        }
        if (request.getEditable() != null) {
            existing.setEditable(request.getEditable());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        SystemSetting saved = systemSettingRepository.save(existing);
        settingsCacheService.reloadCache();
        return SystemSettingMapper.toResponse(saved);
    }

    public SystemSettingResponse getSettingByKey(String key) {
        return systemSettingRepository.findBySettingKey(key)
                .map(SystemSettingMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with key: " + key));
    }

    public List<SystemSettingResponse> getAllSettings() {
        return systemSettingRepository.findAll().stream()
                .map(SystemSettingMapper::toResponse)
                .collect(Collectors.toList());
    }
}

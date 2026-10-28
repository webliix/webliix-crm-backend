package com.webliix.settings.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webliix.settings.dto.NotificationRuleItemDto;
import com.webliix.settings.dto.NotificationRulesConfigDto;
import com.webliix.settings.dto.SystemSettingRequest;
import com.webliix.settings.dto.SystemSettingResponse;
import com.webliix.settings.entity.SystemSetting;
import com.webliix.settings.mapper.SystemSettingMapper;
import com.webliix.settings.repository.SystemSettingRepository;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;
    private final SettingsCacheService settingsCacheService;
    private final ObjectMapper objectMapper;

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

    // =========================================================================
    // NOTIFICATION RULES CONFIGURATION (Settings Page Tab 5)
    // =========================================================================

    @Transactional(readOnly = true)
    public NotificationRulesConfigDto getNotificationConfig() {
        return systemSettingRepository.findBySettingKey("notifications_config")
                .map(setting -> {
                    try {
                        return objectMapper.readValue(setting.getSettingValue(), NotificationRulesConfigDto.class);
                    } catch (Exception e) {
                        log.warn("Failed to parse notifications_config JSON: {}", e.getMessage());
                        return buildDefaultNotificationConfig();
                    }
                })
                .orElseGet(this::buildDefaultNotificationConfig);
    }

    @Transactional
    public NotificationRulesConfigDto saveNotificationConfig(NotificationRulesConfigDto config) {
        try {
            String json = objectMapper.writeValueAsString(config);
            SystemSetting setting = systemSettingRepository.findBySettingKey("notifications_config")
                    .orElseGet(() -> SystemSetting.builder()
                            .settingKey("notifications_config")
                            .settingGroup("NOTIFICATIONS")
                            .dataType("JSON")
                            .description("Global and modular notification & transactional email dispatch rules")
                            .editable(true)
                            .createdAt(LocalDateTime.now())
                            .build());

            setting.setSettingValue(json);
            setting.setUpdatedAt(LocalDateTime.now());
            systemSettingRepository.save(setting);
            settingsCacheService.reloadCache();
            return config;
        } catch (Exception e) {
            log.error("Failed to serialize notifications_config: {}", e.getMessage());
            throw new RuntimeException("Failed to save notification configuration: " + e.getMessage());
        }
    }

    private NotificationRulesConfigDto buildDefaultNotificationConfig() {
        List<NotificationRuleItemDto> defaultRules = new ArrayList<>();
        defaultRules.add(NotificationRuleItemDto.builder()
                .key("lead_created")
                .name("New Inbound Leads & Applications")
                .category("LEADS")
                .description("Trigger instant alerts when a prospective client submits an inquiry or application on webliix.com")
                .emailEnabled(true)
                .inAppEnabled(true)
                .build());

        defaultRules.add(NotificationRuleItemDto.builder()
                .key("lead_assigned")
                .name("Lead Ownership Reassignment")
                .category("LEADS")
                .description("Notify sales engineers when an inquiry is routed to their pipeline queue")
                .emailEnabled(true)
                .inAppEnabled(true)
                .build());

        defaultRules.add(NotificationRuleItemDto.builder()
                .key("project_milestone")
                .name("Project Milestones & Delivery Sign-off")
                .category("PROJECTS")
                .description("Notify clients and project managers when a sprint phase or deliverable is marked completed")
                .emailEnabled(true)
                .inAppEnabled(true)
                .build());

        defaultRules.add(NotificationRuleItemDto.builder()
                .key("invoice_due")
                .name("Invoices, Retainers & Payment Reminders")
                .category("FINANCE")
                .description("Automated transactional statements and milestone payment notices sent to clients")
                .emailEnabled(true)
                .inAppEnabled(true)
                .build());

        defaultRules.add(NotificationRuleItemDto.builder()
                .key("ticket_assigned")
                .name("Support Ticket Assigned to Engineer")
                .category("TICKETS")
                .description("Real-time WebSocket and email dispatch when a customer issue or feature request is assigned")
                .emailEnabled(true)
                .inAppEnabled(true)
                .build());

        defaultRules.add(NotificationRuleItemDto.builder()
                .key("ticket_comment")
                .name("Support Ticket Response & Client Message")
                .category("TICKETS")
                .description("Live alerts when a client or support agent comments on an active ticket")
                .emailEnabled(true)
                .inAppEnabled(true)
                .build());

        defaultRules.add(NotificationRuleItemDto.builder()
                .key("system_alert")
                .name("Security & System Health Alerts")
                .category("SYSTEM")
                .description("Audited admin actions, login security events, and automated maintenance notices")
                .emailEnabled(true)
                .inAppEnabled(true)
                .build());

        return NotificationRulesConfigDto.builder()
                .globalEmailEnabled(true)
                .globalInAppEnabled(true)
                .rules(defaultRules)
                .build();
    }
}

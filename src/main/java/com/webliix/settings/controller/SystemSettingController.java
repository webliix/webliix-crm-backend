package com.webliix.settings.controller;

import com.webliix.security.annotation.RequirePermission;
import com.webliix.settings.dto.SystemSettingRequest;
import com.webliix.settings.dto.SystemSettingResponse;
import com.webliix.settings.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    @PostMapping
    @RequirePermission("SETTINGS_MANAGE")
    public ResponseEntity<SystemSettingResponse> createSetting(@RequestBody SystemSettingRequest request) {
        return ResponseEntity.ok(systemSettingService.createSetting(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("SETTINGS_MANAGE")
    public ResponseEntity<SystemSettingResponse> updateSetting(@PathVariable("id") Long id,
                                                               @RequestBody SystemSettingRequest request) {
        return ResponseEntity.ok(systemSettingService.updateSetting(id, request));
    }

    @GetMapping("/{key}")
    @RequirePermission(anyOf = {"SETTINGS_VIEW", "SETTINGS_MANAGE"})
    public ResponseEntity<SystemSettingResponse> getSetting(@PathVariable("key") String key) {
        return ResponseEntity.ok(systemSettingService.getSettingByKey(key));
    }

    @GetMapping
    @RequirePermission(anyOf = {"SETTINGS_VIEW", "SETTINGS_MANAGE"})
    public ResponseEntity<List<SystemSettingResponse>> getAllSettings() {
        return ResponseEntity.ok(systemSettingService.getAllSettings());
    }
}

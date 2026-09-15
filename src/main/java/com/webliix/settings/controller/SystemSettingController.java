package com.webliix.settings.controller;

import com.webliix.settings.dto.SystemSettingRequest;
import com.webliix.settings.dto.SystemSettingResponse;
import com.webliix.settings.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    @PostMapping
    public ResponseEntity<SystemSettingResponse> createSetting(@RequestBody SystemSettingRequest request) {
        return ResponseEntity.ok(systemSettingService.createSetting(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SystemSettingResponse> updateSetting(@PathVariable("id") Long id,
                                                               @RequestBody SystemSettingRequest request) {
        return ResponseEntity.ok(systemSettingService.updateSetting(id, request));
    }

    @GetMapping("/{key}")
    public ResponseEntity<SystemSettingResponse> getSetting(@PathVariable("key") String key) {
        return ResponseEntity.ok(systemSettingService.getSettingByKey(key));
    }

    @GetMapping
    public ResponseEntity<List<SystemSettingResponse>> getAllSettings() {
        return ResponseEntity.ok(systemSettingService.getAllSettings());
    }
}

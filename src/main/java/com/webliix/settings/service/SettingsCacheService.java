package com.webliix.settings.service;

import com.webliix.settings.entity.SystemSetting;
import com.webliix.settings.repository.SystemSettingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SettingsCacheService {

    private final SystemSettingRepository systemSettingRepository;
    private final Map<String, SystemSetting> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        reloadCache();
    }

    public void reloadCache() {
        cache.clear();
        systemSettingRepository.findAll().forEach(setting -> cache.put(setting.getSettingKey(), setting));
    }

    public SystemSetting getByKey(String key) {
        return cache.get(key);
    }

    public Map<String, SystemSetting> getAll() {
        return Collections.unmodifiableMap(cache);
    }
}

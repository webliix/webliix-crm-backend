package com.webliix.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webliix.security.dto.CachedUserPermissions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPermissionCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "perm:";
    private static final Duration TTL = Duration.ofHours(24);

    public CachedUserPermissions get(Long userId, Integer tokenVersion) {
        if (userId == null || tokenVersion == null) {
            return null;
        }
        try {
            String key = buildKey(userId, tokenVersion);
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json != null && !json.isBlank()) {
                return objectMapper.readValue(json, CachedUserPermissions.class);
            }
        } catch (Exception ex) {
            log.warn("Failed to read user permissions from Redis cache: {}", ex.getMessage());
        }
        return null;
    }

    public void put(Long userId, Integer tokenVersion, CachedUserPermissions permissions) {
        if (userId == null || tokenVersion == null || permissions == null) {
            return;
        }
        try {
            String key = buildKey(userId, tokenVersion);
            String json = objectMapper.writeValueAsString(permissions);
            stringRedisTemplate.opsForValue().set(key, json, TTL);
        } catch (Exception ex) {
            log.warn("Failed to write user permissions to Redis cache: {}", ex.getMessage());
        }
    }

    public void evict(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            String pattern = KEY_PREFIX + userId + ":*";
            Set<String> keys = stringRedisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
                log.debug("Evicted {} permission cache keys for user {}", keys.size(), userId);
            }
        } catch (Exception ex) {
            log.warn("Failed to evict user permissions from Redis cache: {}", ex.getMessage());
        }
    }

    private String buildKey(Long userId, Integer tokenVersion) {
        return KEY_PREFIX + userId + ":" + tokenVersion;
    }
}

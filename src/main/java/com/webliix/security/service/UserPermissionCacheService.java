package com.webliix.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webliix.security.dto.CachedUserPermissions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPermissionCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "perm:";
    private static final Duration TTL = Duration.ofHours(24);
    private static final long L1_TTL_MS = Duration.ofMinutes(5).toMillis();

    // High-performance in-memory L1 cache to shield Redis from repeated roundtrips on every request
    private final Map<String, L1Entry> l1Cache = new ConcurrentHashMap<>();

    private record L1Entry(CachedUserPermissions data, long expiresAt) {
        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    public CachedUserPermissions get(Long userId, Integer tokenVersion) {
        if (userId == null || tokenVersion == null) {
            return null;
        }
        String key = buildKey(userId, tokenVersion);

        // 1. Fast L1 in-memory lookup first (0 network calls to Redis)
        L1Entry l1 = l1Cache.get(key);
        if (l1 != null) {
            if (!l1.isExpired()) {
                return l1.data();
            } else {
                l1Cache.remove(key);
            }
        }

        // 2. Fallback to Redis only on L1 miss
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json != null && !json.isBlank()) {
                CachedUserPermissions parsed = objectMapper.readValue(json, CachedUserPermissions.class);
                l1Cache.put(key, new L1Entry(parsed, System.currentTimeMillis() + L1_TTL_MS));
                return parsed;
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
        String key = buildKey(userId, tokenVersion);
        l1Cache.put(key, new L1Entry(permissions, System.currentTimeMillis() + L1_TTL_MS));

        try {
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
        // Invalidate L1 entries for user
        l1Cache.keySet().removeIf(k -> k.startsWith(KEY_PREFIX + userId + ":"));

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

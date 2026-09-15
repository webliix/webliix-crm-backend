package com.webliix.blog.service;

import com.webliix.blog.repository.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogEngagementService {

    private final BlogPostRepository postRepository;
    private final StringRedisTemplate redisTemplate;

    // In-memory fallback if Redis is unavailable
    private final Map<String, Long> localViewCache = new ConcurrentHashMap<>();
    private final Map<String, Long> localLikeCache = new ConcurrentHashMap<>();

    private static final long VIEW_COOLDOWN_MS = 24 * 60 * 60 * 1000L; // 24 hours
    private static final long LIKE_COOLDOWN_MS = 30L * 24 * 60 * 60 * 1000L; // 30 days

    public boolean recordView(Long postId, String clientIp, String userAgent) {
        String fingerprint = generateFingerprint(clientIp, userAgent);
        String redisKey = "blog:view:" + postId + ":" + fingerprint;

        if (isRedisAvailable()) {
            try {
                Boolean isNew = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", Duration.ofHours(24));
                if (Boolean.TRUE.equals(isNew)) {
                    postRepository.incrementViews(postId);
                    return true;
                }
                return false;
            } catch (Exception ex) {
                log.warn("Redis view record failed, using local cache: {}", ex.getMessage());
            }
        }

        // Local cache fallback
        long now = System.currentTimeMillis();
        Long lastSeen = localViewCache.get(redisKey);
        if (lastSeen == null || (now - lastSeen) > VIEW_COOLDOWN_MS) {
            localViewCache.put(redisKey, now);
            postRepository.incrementViews(postId);
            return true;
        }
        return false;
    }

    public boolean recordLike(Long postId, String clientIp, String userAgent) {
        String fingerprint = generateFingerprint(clientIp, userAgent);
        String redisKey = "blog:like:" + postId + ":" + fingerprint;

        if (isRedisAvailable()) {
            try {
                Boolean isNew = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", Duration.ofDays(30));
                if (Boolean.TRUE.equals(isNew)) {
                    postRepository.incrementLikes(postId);
                    return true;
                }
                return false;
            } catch (Exception ex) {
                log.warn("Redis like record failed, using local cache: {}", ex.getMessage());
            }
        }

        // Local cache fallback
        long now = System.currentTimeMillis();
        Long lastSeen = localLikeCache.get(redisKey);
        if (lastSeen == null || (now - lastSeen) > LIKE_COOLDOWN_MS) {
            localLikeCache.put(redisKey, now);
            postRepository.incrementLikes(postId);
            return true;
        }
        return false;
    }

    private boolean isRedisAvailable() {
        if (redisTemplate == null) return false;
        try {
            return redisTemplate.getConnectionFactory() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private String generateFingerprint(String clientIp, String userAgent) {
        try {
            String raw = (clientIp != null ? clientIp : "127.0.0.1") + "|" + (userAgent != null ? userAgent : "unknown");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 16);
        } catch (Exception e) {
            return "anon-" + (clientIp != null ? clientIp.hashCode() : "0");
        }
    }
}

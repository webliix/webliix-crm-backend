package com.webliix.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisRateLimiterService implements RateLimiterService {

    private final RedisTemplate<String, Object> redisTemplate;

    // In-memory fallback tracking map when Redis connection is unavailable
    private final Map<String, RateLimitEntry> localLimiters = new ConcurrentHashMap<>();

    private static class RateLimitEntry {
        long count;
        long expireTimeMs;

        RateLimitEntry(long count, long expireTimeMs) {
            this.count = count;
            this.expireTimeMs = expireTimeMs;
        }
    }

    @Override
    public void checkRateLimit(String key, int maxRequests, long windowSeconds, String actionName) {
        String redisKey = "webliix:ratelimit:" + key;

        try {
            Long currentCount = redisTemplate.opsForValue().increment(redisKey);
            if (currentCount == null) {
                currentCount = 1L;
            }

            if (currentCount == 1L) {
                redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
            }

            if (currentCount > maxRequests) {
                Long ttl = redisTemplate.getExpire(redisKey);
                long waitSec = ttl != null && ttl > 0 ? ttl : windowSeconds;
                log.warn("Rate limit exceeded for action [{}] on key [{}]. Count: {}, Max: {}", actionName, key, currentCount, maxRequests);
                throw new IllegalArgumentException("Too many " + actionName + " attempts. Please try again in " + waitSec + " seconds.");
            }
            return;
        } catch (IllegalArgumentException ex) {
            throw ex; // Re-throw intentional rate limit violation
        } catch (Exception ex) {
            log.warn("Redis unavailable for rate limiter [{}]: {}. Falling back to in-memory rate limiting.", actionName, ex.getMessage());
        }

        // In-Memory Fallback Rate Limiting
        checkLocalRateLimit(redisKey, maxRequests, windowSeconds, actionName);
    }

    private void checkLocalRateLimit(String key, int maxRequests, long windowSeconds, String actionName) {
        long now = System.currentTimeMillis();
        long windowMs = windowSeconds * 1000L;

        RateLimitEntry entry = localLimiters.compute(key, (k, existing) -> {
            if (existing == null || now > existing.expireTimeMs) {
                return new RateLimitEntry(1L, now + windowMs);
            } else {
                existing.count++;
                return existing;
            }
        });

        if (entry.count > maxRequests) {
            long waitSec = Math.max(1, (entry.expireTimeMs - now) / 1000L);
            log.warn("In-memory rate limit exceeded for action [{}] on key [{}]. Count: {}, Max: {}", actionName, key, entry.count, maxRequests);
            throw new IllegalArgumentException("Too many " + actionName + " attempts. Please try again in " + waitSec + " seconds.");
        }
    }
}

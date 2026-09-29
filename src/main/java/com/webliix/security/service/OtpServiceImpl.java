package com.webliix.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    // In-memory store fallback when Redis is offline/unreachable
    private final Map<String, InMemoryEntry> localOtpStore = new ConcurrentHashMap<>();

    private static class InMemoryEntry {
        Object value;
        long expiresAtMs;

        InMemoryEntry(Object value, long ttlMs) {
            this.value = value;
            this.expiresAtMs = System.currentTimeMillis() + ttlMs;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAtMs;
        }

        long getTtlSeconds() {
            long remaining = (expiresAtMs - System.currentTimeMillis()) / 1000L;
            return Math.max(0, remaining);
        }
    }

    @Value("${webliix.otp.length:6}")
    private int otpLength;

    @Value("${webliix.otp.expiration-seconds:300}")
    private long expirationSeconds;

    @Value("${webliix.otp.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    @Value("${webliix.otp.max-attempts:5}")
    private int maxAttempts;

    @Override
    public void checkResendCooldown(String identifier) {
        String cooldownKey = PREFIX_RESEND_COOLDOWN + normalizeIdentifier(identifier);
        Long ttl = getExpireKey(cooldownKey);
        if (ttl != null && ttl > 0) {
            throw new IllegalArgumentException("Please wait " + ttl + " seconds before requesting another code.");
        }
    }

    @Override
    public String generateAndStoreOtp(String keyPrefix, String identifier) {
        String normalizedId = normalizeIdentifier(identifier);
        checkResendCooldown(normalizedId);

        // Generate 6-digit numeric string
        String rawOtp = generateNumericOtp(otpLength);
        String hashedOtp = hashOtp(rawOtp);

        String otpKey = keyPrefix + normalizedId;
        String cooldownKey = PREFIX_RESEND_COOLDOWN + normalizedId;
        String attemptsKey = PREFIX_ATTEMPTS + normalizedId;

        // Store hashed OTP with expiration (5 minutes)
        setKey(otpKey, hashedOtp, Duration.ofSeconds(expirationSeconds));

        // Store resend cooldown (60 seconds)
        setKey(cooldownKey, "1", Duration.ofSeconds(resendCooldownSeconds));

        // Reset attempt count
        deleteKey(attemptsKey);

        log.info("Generated OTP for identifier: [PROTECTED], Scope: {}, TTL: {}s", keyPrefix, expirationSeconds);
        return rawOtp;
    }

    @Override
    public boolean verifyOtp(String keyPrefix, String identifier, String candidateOtp) {
        if (candidateOtp == null || candidateOtp.isBlank()) {
            return false;
        }

        String normalizedId = normalizeIdentifier(identifier);
        String otpKey = keyPrefix + normalizedId;
        String attemptsKey = PREFIX_ATTEMPTS + normalizedId;

        // Check current attempts
        Object attemptsObj = getKey(attemptsKey);
        int currentAttempts = attemptsObj != null ? Integer.parseInt(attemptsObj.toString()) : 0;

        if (currentAttempts >= maxAttempts) {
            deleteKey(otpKey);
            log.warn("Max OTP verification attempts exceeded for scope: {}", keyPrefix);
            throw new IllegalArgumentException("Maximum verification attempts exceeded. Please request a new verification code.");
        }

        Object storedHashObj = getKey(otpKey);
        if (storedHashObj == null) {
            return false;
        }

        String candidateHash = hashOtp(candidateOtp.trim());
        boolean matches = candidateHash.equals(storedHashObj.toString());

        if (matches) {
            // Invalidate OTP immediately after successful single-use verification
            deleteKey(otpKey);
            deleteKey(attemptsKey);
            log.info("OTP successfully verified for scope: {}", keyPrefix);
            return true;
        } else {
            // Increment failed attempt count
            long updatedAttempts = incrementKey(attemptsKey);
            setKeyTtl(attemptsKey, Duration.ofSeconds(expirationSeconds));
            int remaining = maxAttempts - (int) updatedAttempts;
            log.warn("Failed OTP verification attempt. Remaining attempts: {}", remaining);
            return false;
        }
    }

    @Override
    public String createResetToken(String identifier) {
        String normalizedId = normalizeIdentifier(identifier);
        String resetToken = UUID.randomUUID().toString().replace("-", "");
        String tokenKey = PREFIX_RESET_TOKEN + normalizedId;

        // Valid for 10 minutes to complete password reset screen
        setKey(tokenKey, resetToken, Duration.ofMinutes(10));
        return resetToken;
    }

    @Override
    public boolean verifyResetToken(String identifier, String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String normalizedId = normalizeIdentifier(identifier);
        String tokenKey = PREFIX_RESET_TOKEN + normalizedId;
        Object storedToken = getKey(tokenKey);
        return storedToken != null && storedToken.toString().equals(token.trim());
    }

    @Override
    public void clearResetToken(String identifier) {
        String normalizedId = normalizeIdentifier(identifier);
        String tokenKey = PREFIX_RESET_TOKEN + normalizedId;
        deleteKey(tokenKey);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Resilient Redis Operation Wrappers with In-Memory Fallback
    // ─────────────────────────────────────────────────────────────────────────

    private Long getExpireKey(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key);
            if (ttl != null) return ttl;
        } catch (Exception ex) {
            log.warn("Redis getExpire failed for key [{}]: {}. Using local memory.", key, ex.getMessage());
        }
        InMemoryEntry entry = localOtpStore.get(key);
        if (entry != null && !entry.isExpired()) {
            return entry.getTtlSeconds();
        }
        return null;
    }

    private Object getKey(String key) {
        try {
            Object val = redisTemplate.opsForValue().get(key);
            if (val != null) return val;
        } catch (Exception ex) {
            log.warn("Redis get failed for key [{}]: {}. Using local memory.", key, ex.getMessage());
        }
        InMemoryEntry entry = localOtpStore.get(key);
        if (entry != null) {
            if (entry.isExpired()) {
                localOtpStore.remove(key);
                return null;
            }
            return entry.value;
        }
        return null;
    }

    private void setKey(String key, Object value, Duration duration) {
        try {
            redisTemplate.opsForValue().set(key, value, duration);
        } catch (Exception ex) {
            log.warn("Redis set failed for key [{}]: {}. Storing in local memory.", key, ex.getMessage());
        }
        localOtpStore.put(key, new InMemoryEntry(value, duration.toMillis()));
    }

    private void deleteKey(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception ex) {
            log.warn("Redis delete failed for key [{}]: {}. Removing from local memory.", key, ex.getMessage());
        }
        localOtpStore.remove(key);
    }

    private long incrementKey(String key) {
        try {
            Long val = redisTemplate.opsForValue().increment(key);
            if (val != null) return val;
        } catch (Exception ex) {
            log.warn("Redis increment failed for key [{}]: {}. Using local memory.", key, ex.getMessage());
        }
        InMemoryEntry entry = localOtpStore.get(key);
        long current = 0;
        if (entry != null && !entry.isExpired()) {
            try {
                current = Long.parseLong(entry.value.toString());
            } catch (Exception ignored) {}
        }
        long next = current + 1;
        localOtpStore.put(key, new InMemoryEntry(next, Duration.ofSeconds(expirationSeconds).toMillis()));
        return next;
    }

    private void setKeyTtl(String key, Duration duration) {
        try {
            redisTemplate.expire(key, duration);
        } catch (Exception ex) {
            log.warn("Redis expire failed for key [{}]: {}", key, ex.getMessage());
        }
    }

    private String normalizeIdentifier(String identifier) {
        return identifier != null ? identifier.trim().toLowerCase() : "";
    }

    private String generateNumericOtp(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

    private String hashOtp(String otp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(otp.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}

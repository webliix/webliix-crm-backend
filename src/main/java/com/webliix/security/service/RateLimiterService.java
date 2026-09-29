package com.webliix.security.service;

public interface RateLimiterService {

    /**
     * Check rate limit for an action. Throws IllegalArgumentException if limit exceeded.
     */
    void checkRateLimit(String key, int maxRequests, long windowSeconds, String actionName);
}

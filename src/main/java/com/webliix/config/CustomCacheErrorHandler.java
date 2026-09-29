package com.webliix.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

@Slf4j
public class CustomCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Redis cache get failed for key [{}] on cache [{}]: {}. Bypassing cache.", key, cache.getName(), exception.getMessage());
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn("Redis cache put failed for key [{}] on cache [{}]: {}. Bypassing cache.", key, cache.getName(), exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Redis cache evict failed for key [{}] on cache [{}]: {}. Bypassing cache.", key, cache.getName(), exception.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn("Redis cache clear failed on cache [{}]: {}. Bypassing cache.", cache.getName(), exception.getMessage());
    }
}

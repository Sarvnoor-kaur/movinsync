package com.fleetbilling.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.stereotype.Component;

/**
 * Custom CacheErrorHandler that gracefully handles Redis connection failures or timeouts.
 *
 * If Redis is unavailable, cache operations log a warning and fallback directly to MySQL database reads
 * without interrupting API calls or returning 500 Internal Server Errors.
 */
@Slf4j
@Component
public class RedisCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Redis Cache GET failed for key '{}' in cache '{}'; falling back to database: {}",
                key, cache.getName(), exception.getMessage());
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn("Redis Cache PUT failed for key '{}' in cache '{}'; proceeding without caching: {}",
                key, cache.getName(), exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Redis Cache EVICT failed for key '{}' in cache '{}': {}",
                key, cache.getName(), exception.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn("Redis Cache CLEAR failed for cache '{}': {}",
                cache.getName(), exception.getMessage());
    }
}

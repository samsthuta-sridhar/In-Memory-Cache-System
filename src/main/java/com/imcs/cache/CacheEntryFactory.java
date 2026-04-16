package com.imcs.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CacheEntryFactory {

    @Value("${cache.ttl-ms:5000}")
    private long defaultTtl;

    // Creator pattern — only class that instantiates CacheEntry
    public CacheEntry create(String key, String value) {
        return new CacheEntry(key, value, defaultTtl);
    }

    public CacheEntry create(String key, String value, long customTtl) {
        return new CacheEntry(key, value, customTtl);
    }
}
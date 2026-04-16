package com.imcs.eviction;

import com.imcs.cache.CacheEntry;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;

@Component("lru")
public class LRUEvictionPolicy implements EvictionPolicy {

    @Override
    public String selectEvictionKey(Map<String, CacheEntry> cacheStore) {
        return cacheStore.entrySet().stream()
                .min(Comparator.comparingLong(e -> e.getValue().getLastAccessTime()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
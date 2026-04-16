package com.imcs.eviction;

import com.imcs.cache.CacheEntry;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;

@Component("lfu")
public class LFUEvictionPolicy implements EvictionPolicy {

    @Override
    public String selectEvictionKey(Map<String, CacheEntry> cacheStore) {
        return cacheStore.entrySet().stream()
                .min(Comparator.comparingInt(e -> e.getValue().getAccessCount()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
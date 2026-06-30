package com.imcs.eviction;

import com.imcs.cache.CacheEntry;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;

/**
 * First-In-First-Out eviction policy.
 *
 * Evicts the entry that was inserted earliest, regardless of how
 * often or how recently it has been accessed. This demonstrates
 * the Open/Closed Principle: this class is added without modifying
 * CacheManager, CacheService, or any existing eviction policy.
 */
@Component("fifo")
public class FIFOEvictionPolicy implements EvictionPolicy {

    @Override
    public String selectEvictionKey(Map<String, CacheEntry> cacheStore) {
        return cacheStore.entrySet().stream()
            .min(Comparator.comparingLong(e -> e.getValue().getExpiryTime()))
            .map(Map.Entry::getKey)
            .orElse(null);
    }
}
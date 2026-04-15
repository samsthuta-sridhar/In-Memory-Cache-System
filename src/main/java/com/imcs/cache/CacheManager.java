package com.imcs.cache;

import com.imcs.eviction.EvictionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CacheManager {

    private final Map<String, CacheEntry> cacheStore = new ConcurrentHashMap<>();

    @Value("${cache.max-capacity:10}")
    private int maxCapacity;

    @Autowired
    private EvictionPolicy evictionPolicy;

    private String lastEvictedKey = null;
    private String lastEvictedValue = null;
    private String lastEvictionPolicyName = null;

    public CacheEntry getEntry(String key) {
        CacheEntry entry = cacheStore.get(key);
        if (entry != null && entry.isExpired()) {
            cacheStore.remove(key);
            return null;
        }
        return entry;
    }

    // Returns evicted key if eviction happened, null otherwise
    public String putEntry(String key, CacheEntry entry) {
        String evictedKey = null;
        if (isCacheFull() && !cacheStore.containsKey(key)) {
            evictedKey = evictOne();
        }
        cacheStore.put(key, entry);
        return evictedKey;
    }

    public void removeEntry(String key) {
        cacheStore.remove(key);
    }

    public boolean containsKey(String key) {
        return cacheStore.containsKey(key);
    }

    public boolean isCacheFull() {
        return cacheStore.size() >= maxCapacity;
    }

    public int getCurrentSize() {
        return cacheStore.size();
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public Map<String, CacheEntry> getAllEntries() {
        return Collections.unmodifiableMap(cacheStore);
    }

    public void setEvictionPolicy(EvictionPolicy policy) {
        this.evictionPolicy = policy;
    }

    public EvictionPolicy getEvictionPolicy() {
        return evictionPolicy;
    }

    public String getLastEvictedKey() { return lastEvictedKey; }
    public String getLastEvictedValue() { return lastEvictedValue; }
    public String getLastEvictionPolicyName() { return lastEvictionPolicyName; }

    private String evictOne() {
        String keyToEvict = evictionPolicy.selectEvictionKey(cacheStore);
        if (keyToEvict != null) {
            CacheEntry evicted = cacheStore.get(keyToEvict);
            lastEvictedKey = keyToEvict;
            lastEvictedValue = evicted != null ? evicted.getValue() : "unknown";
            lastEvictionPolicyName = evictionPolicy.getClass()
                .getSimpleName().replace("EvictionPolicy", "");
            cacheStore.remove(keyToEvict);
        }
        return keyToEvict;
    }
}
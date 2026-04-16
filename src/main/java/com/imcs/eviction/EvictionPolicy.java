package com.imcs.eviction;

import com.imcs.cache.CacheEntry;
import java.util.Map;

public interface EvictionPolicy {
    String selectEvictionKey(Map<String, CacheEntry> cacheStore);
}
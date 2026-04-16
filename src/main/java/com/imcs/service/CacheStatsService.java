package com.imcs.service;

import com.imcs.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CacheStatsService {

    @Autowired private CacheManager cacheManager;

    @Value("${cache.max-capacity:10}")
    private int maxCapacity;

    private final AtomicInteger hitCount = new AtomicInteger(0);
    private final AtomicInteger missCount = new AtomicInteger(0);
    private final AtomicInteger evictionCount = new AtomicInteger(0);
    private final AtomicInteger totalStored = new AtomicInteger(0);

    public void recordHit() { hitCount.incrementAndGet(); }
    public void recordMiss() { missCount.incrementAndGet(); }
    public void recordEviction() { evictionCount.incrementAndGet(); }
    public void recordStore() { totalStored.incrementAndGet(); }

    public int getHitCount() { return hitCount.get(); }
    public int getMissCount() { return missCount.get(); }
    public int getEvictionCount() { return evictionCount.get(); }
    public int getTotalStored() { return totalStored.get(); }
    public int getMaxCapacity() { return maxCapacity; }

    // Always read live from CacheManager
    public int getCurrentSize() {
        return cacheManager.getAllEntries().size();
    }

    public double getHitRate() {
        int total = hitCount.get() + missCount.get();
        if (total == 0) return 0.0;
        return (hitCount.get() * 100.0) / total;
    }

    public double getCapacityPercent() {
        return (getCurrentSize() * 100.0) / maxCapacity;
    }

    public void reset() {
        hitCount.set(0);
        missCount.set(0);
        evictionCount.set(0);
        totalStored.set(0);
    }
}
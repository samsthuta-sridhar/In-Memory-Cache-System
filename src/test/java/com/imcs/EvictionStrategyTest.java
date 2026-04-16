package com.imcs;

import com.imcs.cache.CacheEntry;
import com.imcs.eviction.LFUEvictionPolicy;
import com.imcs.eviction.LRUEvictionPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class EvictionStrategyTest {

    private final LRUEvictionPolicy lruPolicy = new LRUEvictionPolicy();
    private final LFUEvictionPolicy lfuPolicy = new LFUEvictionPolicy();

    @Test
    void testLRUEvictsLeastRecentlyUsed() throws InterruptedException {
        Map<String, CacheEntry> cache = new HashMap<>();

        CacheEntry entry1 = new CacheEntry("key1", "val1", 60000);
        Thread.sleep(10);
        CacheEntry entry2 = new CacheEntry("key2", "val2", 60000);
        Thread.sleep(10);
        CacheEntry entry3 = new CacheEntry("key3", "val3", 60000);

        cache.put("key1", entry1);
        cache.put("key2", entry2);
        cache.put("key3", entry3);

        String evicted = lruPolicy.selectEvictionKey(cache);
        assertEquals("key1", evicted, "LRU should evict the oldest entry");
    }

    @Test
    void testLFUEvictsLeastFrequentlyUsed() {
        Map<String, CacheEntry> cache = new HashMap<>();

        CacheEntry entry1 = new CacheEntry("key1", "val1", 60000);
        CacheEntry entry2 = new CacheEntry("key2", "val2", 60000);
        CacheEntry entry3 = new CacheEntry("key3", "val3", 60000);

        // entry2 and entry3 accessed more
        entry2.updateAccessMetadata();
        entry2.updateAccessMetadata();
        entry3.updateAccessMetadata();

        cache.put("key1", entry1);
        cache.put("key2", entry2);
        cache.put("key3", entry3);

        String evicted = lfuPolicy.selectEvictionKey(cache);
        assertEquals("key1", evicted, "LFU should evict least accessed entry");
    }

    @Test
    void testLRUWithEmptyCache() {
        Map<String, CacheEntry> cache = new HashMap<>();
        String evicted = lruPolicy.selectEvictionKey(cache);
        assertNull(evicted, "LRU should return null for empty cache");
    }

    @Test
    void testLFUWithEmptyCache() {
        Map<String, CacheEntry> cache = new HashMap<>();
        String evicted = lfuPolicy.selectEvictionKey(cache);
        assertNull(evicted, "LFU should return null for empty cache");
    }

    @Test
    void testLRUWithSingleEntry() {
        Map<String, CacheEntry> cache = new HashMap<>();
        cache.put("only", new CacheEntry("only", "value", 60000));
        String evicted = lruPolicy.selectEvictionKey(cache);
        assertEquals("only", evicted);
    }
}
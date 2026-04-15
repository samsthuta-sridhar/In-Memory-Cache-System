package com.imcs;

import com.imcs.cache.CacheEntry;
import com.imcs.cache.CacheManager;
import com.imcs.cache.CacheEntryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CacheRetrievalTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CacheEntryFactory cacheEntryFactory;

    @BeforeEach
    void setUp() {
        cacheManager.removeEntry("test:001");
        cacheManager.removeEntry("test:002");
    }

    @Test
    void testCacheHit() {
        CacheEntry entry = cacheEntryFactory.create("test:001", "balance:5000");
        cacheManager.putEntry("test:001", entry);

        CacheEntry result = cacheManager.getEntry("test:001");
        assertNotNull(result, "Entry should be found in cache");
        assertEquals("balance:5000", result.getValue());
    }

    @Test
    void testCacheMiss() {
        CacheEntry result = cacheManager.getEntry("nonexistent:key");
        assertNull(result, "Non-existent key should return null");
    }

    @Test
    void testContainsKey() {
        CacheEntry entry = cacheEntryFactory.create("test:002", "balance:1000");
        cacheManager.putEntry("test:002", entry);

        assertTrue(cacheManager.containsKey("test:002"));
        assertFalse(cacheManager.containsKey("test:999"));
    }

    @Test
    void testAccessMetadataUpdated() {
        CacheEntry entry = cacheEntryFactory.create("test:001", "balance:5000");
        cacheManager.putEntry("test:001", entry);

        CacheEntry result = cacheManager.getEntry("test:001");
        assertNotNull(result);
        int before = result.getAccessCount();
        result.updateAccessMetadata();
        assertEquals(before + 1, result.getAccessCount());
    }

    @Test
    void testRemoveEntry() {
        CacheEntry entry = cacheEntryFactory.create("test:001", "balance:5000");
        cacheManager.putEntry("test:001", entry);
        cacheManager.removeEntry("test:001");
        assertNull(cacheManager.getEntry("test:001"));
    }
}
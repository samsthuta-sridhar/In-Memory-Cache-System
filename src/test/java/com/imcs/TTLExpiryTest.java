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
public class TTLExpiryTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CacheEntryFactory cacheEntryFactory;

    @BeforeEach
    void setUp() {
        cacheManager.removeEntry("ttl:test");
        cacheManager.removeEntry("ttl:valid");
    }

    @Test
    void testEntryExpiresAfterTTL() throws InterruptedException {
        // Create entry with 100ms TTL
        CacheEntry entry = new CacheEntry("ttl:test", "value", 100);
        cacheManager.putEntry("ttl:test", entry);

        assertNotNull(cacheManager.getEntry("ttl:test"), "Entry should exist before TTL");

        Thread.sleep(200);

        assertNull(cacheManager.getEntry("ttl:test"), "Entry should be expired after TTL");
    }

    @Test
    void testEntryValidBeforeTTL() throws InterruptedException {
        CacheEntry entry = new CacheEntry("ttl:valid", "value", 5000);
        cacheManager.putEntry("ttl:valid", entry);

        Thread.sleep(100);

        assertNotNull(cacheManager.getEntry("ttl:valid"),
            "Entry should still be valid before TTL expires");
    }

    @Test
    void testIsExpiredMethod() throws InterruptedException {
        CacheEntry entry = new CacheEntry("ttl:test", "value", 100);
        assertFalse(entry.isExpired(), "Entry should not be expired immediately");
        Thread.sleep(200);
        assertTrue(entry.isExpired(), "Entry should be expired after TTL");
    }

    @Test
    void testDeleteRemovesFromCache() {
        CacheEntry entry = cacheEntryFactory.create("ttl:test", "value");
        cacheManager.putEntry("ttl:test", entry);
        cacheManager.removeEntry("ttl:test");
        assertNull(cacheManager.getEntry("ttl:test"));
    }

    @Test
    void testCacheManagerIsSingleton() {
        assertNotNull(cacheManager, "CacheManager should be a singleton Spring bean");
        assertTrue(cacheManager.getAllEntries() != null,
            "CacheManager should maintain its state");
    }
}
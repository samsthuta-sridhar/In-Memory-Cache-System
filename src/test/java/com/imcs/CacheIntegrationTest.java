package com.imcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.imcs.cache.CacheManager;
import com.imcs.repository.CacheDataRepository;
import com.imcs.service.CacheService;

@SpringBootTest
public class CacheIntegrationTest {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CacheDataRepository cacheDataRepository;

    @BeforeEach
    void setUp() {
        cacheManager.removeEntry("integration:001");
        cacheDataRepository.deleteById("integration:001");
    }

    @Test
    void testFullPutAndRetrieveLifecycle() {
        cacheService.put("integration:001", "balance:9000");

        String value = cacheService.getValue("integration:001");
        assertNotNull(value);
        assertEquals("balance:9000", value);
    }

    @Test
    void testUpdateOverwritesValue() {
        cacheService.put("integration:001", "balance:1000");
        cacheService.update("integration:001", "balance:2000");

        String value = cacheService.getValue("integration:001");
        assertEquals("balance:2000", value);
    }

    @Test
    void testDeleteRemovesFromCacheAndDB() {
        cacheService.put("integration:001", "balance:5000");
        cacheService.delete("integration:001");

        assertNull(cacheService.getValue("integration:001"));
        assertFalse(cacheDataRepository.findById("integration:001").isPresent());
    }

    @Test
    void testCacheHitAfterPut() {
        cacheService.put("integration:001", "balance:7777");
        assertTrue(cacheManager.containsKey("integration:001"),
            "Cache should contain key after put");
    }

    @Test
    void testDataPersistedInDatabase() {
        cacheService.put("integration:001", "balance:3333");
        assertTrue(cacheDataRepository.findById("integration:001").isPresent(),
            "Data should be persisted in PostgreSQL");
    }
}
package com.imcs;

import com.imcs.cache.CacheManager;
import com.imcs.entity.CacheDataEntity;
import com.imcs.repository.CacheDataRepository;
import com.imcs.service.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CacheMissFallbackTest {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CacheDataRepository cacheDataRepository;

    @BeforeEach
    void setUp() {
        cacheManager.removeEntry("account:fallback");
        cacheDataRepository.deleteById("account:fallback");
    }

    @Test
    void testCacheMissFallsBackToDatabase() {
        // Store directly in DB only
        cacheDataRepository.save(new CacheDataEntity(
            "account:fallback",
            encryptForTest("balance:9999")));

        // Should fetch from DB and cache it
        String value = cacheService.getValue("account:fallback");
        assertNotNull(value, "Should retrieve from DB on cache miss");
        assertTrue(value.contains("9999"));
    }

    @Test
    void testCacheMissReturnsNullWhenNotInDB() {
        String value = cacheService.getValue("account:doesnotexist");
        assertNull(value, "Should return null if not in cache or DB");
    }

    @Test
    void testAfterMissCacheIsPopulated() {
        cacheDataRepository.save(new CacheDataEntity(
            "account:fallback",
            encryptForTest("balance:7777")));

        cacheService.getValue("account:fallback");
        assertTrue(cacheManager.containsKey("account:fallback"),
            "Cache should be populated after DB fallback");
    }

    @Test
    void testPutStoresInCacheAndDB() {
        cacheService.put("account:fallback", "balance:1234");
        assertTrue(cacheManager.containsKey("account:fallback"));
        assertTrue(cacheDataRepository.findById("account:fallback").isPresent());
    }

    // Helper to encrypt for test setup
    private String encryptForTest(String value) {
        try {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding");
            byte[] keyBytes = "imcs1234567890ab".getBytes();
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE,
                new javax.crypto.spec.SecretKeySpec(keyBytes, "AES"));
            return java.util.Base64.getEncoder()
                .encodeToString(cipher.doFinal(value.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
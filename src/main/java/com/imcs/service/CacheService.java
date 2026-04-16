package com.imcs.service;

import com.imcs.cache.CacheEntry;
import com.imcs.cache.CacheEntryFactory;
import com.imcs.cache.CacheManager;
import com.imcs.entity.CacheDataEntity;
import com.imcs.eviction.EvictionPolicy;
import com.imcs.eviction.LFUEvictionPolicy;
import com.imcs.eviction.LRUEvictionPolicy;
import com.imcs.repository.CacheDataRepository;
import com.imcs.security.EncryptionService;
import com.imcs.security.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CacheService {

    @Autowired private CacheManager cacheManager;
    @Autowired private CacheDataRepository cacheDataRepository;
    @Autowired private CacheEntryFactory cacheEntryFactory;
    @Autowired private CacheStatsService statsService;
    @Autowired private EncryptionService encryptionService;
    @Autowired private RateLimiterService rateLimiterService;
    @Autowired private AuditLogService auditLogService;

    @Autowired @Qualifier("lru") private EvictionPolicy lruEvictionPolicy;
    @Autowired @Qualifier("lfu") private EvictionPolicy lfuEvictionPolicy;

    public String getValue(String key) {
        if (!rateLimiterService.isAllowed(key)) {
            auditLogService.log("GET", key, "RATE_LIMITED");
            return "RATE_LIMITED";
        }

        CacheEntry entry = cacheManager.getEntry(key);
        if (entry != null) {
            entry.updateAccessMetadata();
            statsService.recordHit();
            auditLogService.log("GET", key,
                "CACHE_HIT | access count: " + entry.getAccessCount());
            return encryptionService.decrypt(entry.getValue());
        }

        statsService.recordMiss();
        Optional<CacheDataEntity> dbEntry = cacheDataRepository.findById(key);
        if (dbEntry.isPresent()) {
            String storedValue = dbEntry.get().getValue();
            CacheEntry newEntry = cacheEntryFactory.create(key, storedValue);
            cacheManager.putEntry(key, newEntry);
            auditLogService.log("GET", key, "CACHE_MISS → DB_FALLBACK_HIT");
            return encryptionService.decrypt(storedValue);
        }

        auditLogService.log("GET", key, "NOT_FOUND in cache or DB");
        return null;
    }

    public PutResult put(String key, String value) {
        String encrypted = encryptionService.encrypt(value);
        CacheEntry entry = cacheEntryFactory.create(key, encrypted);

        boolean willEvict = cacheManager.isCacheFull()
            && !cacheManager.containsKey(key);
        String policyName = getCurrentPolicy();

        String evictedKey = cacheManager.putEntry(key, entry);
        cacheDataRepository.save(new CacheDataEntity(key, encrypted));
        statsService.recordStore();

        if (evictedKey != null) {
            statsService.recordEviction();
            String evictedEncrypted = cacheManager.getLastEvictedValue();
            String evictedDecrypted;
            try {
                evictedDecrypted = encryptionService.decrypt(evictedEncrypted);
            } catch (Exception e) {
                evictedDecrypted = "(unavailable)";
            }
            auditLogService.log("EVICT", evictedKey,
                "Evicted by " + policyName
                + " policy to make room for " + key);
            auditLogService.log("PUT", key,
                "SUCCESS (after eviction of " + evictedKey + ")");
            return new PutResult(true, evictedKey,
                evictedDecrypted, policyName);
        }

        auditLogService.log("PUT", key,
            "SUCCESS | cache size: "
            + cacheManager.getCurrentSize()
            + "/" + cacheManager.getMaxCapacity());
        return new PutResult(false, null, null, policyName);
    }

    public void update(String key, String newValue) {
        String encrypted = encryptionService.encrypt(newValue);
        cacheDataRepository.save(new CacheDataEntity(key, encrypted));
        CacheEntry entry = cacheEntryFactory.create(key, encrypted);
        cacheManager.putEntry(key, entry);
        auditLogService.log("UPDATE", key, "SUCCESS | new value stored");
    }

    public void delete(String key) {
        cacheManager.removeEntry(key);
        cacheDataRepository.deleteById(key);
        auditLogService.log("DELETE", key,
            "SUCCESS | removed from cache and DB");
    }

    public void setEvictionPolicy(String policy) {
        if ("LFU".equalsIgnoreCase(policy)) {
            cacheManager.setEvictionPolicy(lfuEvictionPolicy);
        } else {
            cacheManager.setEvictionPolicy(lruEvictionPolicy);
        }
        auditLogService.log("CONFIG", "eviction-policy",
            "Changed to " + policy.toUpperCase());
    }

    public String getCurrentPolicy() {
        return cacheManager.getEvictionPolicy()
            instanceof LFUEvictionPolicy ? "LFU" : "LRU";
    }

    // Result object carrying eviction info back to controller
    public static class PutResult {
        public final boolean evictionHappened;
        public final String evictedKey;
        public final String evictedValue;
        public final String evictionPolicy;

        public PutResult(boolean evictionHappened, String evictedKey,
                         String evictedValue, String evictionPolicy) {
            this.evictionHappened = evictionHappened;
            this.evictedKey = evictedKey;
            this.evictedValue = evictedValue;
            this.evictionPolicy = evictionPolicy;
        }
    }
}
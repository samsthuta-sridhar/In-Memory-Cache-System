package com.imcs.events;

import com.imcs.service.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CacheExpiryListener {

    private static final Logger log = LoggerFactory.getLogger(CacheExpiryListener.class);

    @Autowired
    private AuditLogService auditLogService;

    // Observer pattern — reacts to TTL expiry events
    @EventListener
    public void onCacheExpiry(CacheExpiryEvent event) {
        String key = event.getCacheKey();
        // Only log — do NOT delete from DB
        // DB is the persistent store, cache is temporary
        // Next GET will trigger DB fallback and re-cache the entry
        auditLogService.log("TTL_EXPIRE", key,
            "Expired from cache — still available in DB");
        log.info("TTL expired for: {} — removed from cache only", key);
    }
}
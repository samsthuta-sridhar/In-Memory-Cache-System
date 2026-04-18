package com.imcs.events;

import com.imcs.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CacheExpiryListener {

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
        System.out.println("TTL expired for: " + key + " — removed from cache only");
    }
}
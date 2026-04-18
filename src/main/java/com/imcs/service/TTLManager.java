package com.imcs.service;

import com.imcs.cache.CacheManager;
import com.imcs.events.CacheExpiryEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TTLManager {

    // Low Coupling — only depends on CacheManager + event publisher
    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedRateString = "${cache.ttl-ms:5000}")
    public void checkAndExpireEntries() {
        cacheManager.getAllEntries().forEach((key, entry) -> {
            if (entry.isExpired()) {
                cacheManager.removeEntry(key);
                eventPublisher.publishEvent(new CacheExpiryEvent(this, key));
            }
        });
    }
}
package com.imcs.events;

import org.springframework.context.ApplicationEvent;

public class CacheExpiryEvent extends ApplicationEvent {

    private final String cacheKey;

    public CacheExpiryEvent(Object source, String cacheKey) {
        super(source);
        this.cacheKey = cacheKey;
    }

    public String getCacheKey() {
        return cacheKey;
    }
}
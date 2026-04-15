package com.imcs.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimiterService {

    @Value("${security.rate-limit-per-second:10}")
    private int maxRequestsPerSecond;

    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> windowStart = new ConcurrentHashMap<>();

    public boolean isAllowed(String key) {
        long now = System.currentTimeMillis();
        windowStart.putIfAbsent(key, now);
        requestCounts.putIfAbsent(key, new AtomicInteger(0));

        long windowTime = windowStart.get(key);
        if (now - windowTime > 1000) {
            windowStart.put(key, now);
            requestCounts.get(key).set(0);
        }

        return requestCounts.get(key).incrementAndGet() <= maxRequestsPerSecond;
    }

    public int getRequestCount(String key) {
        return requestCounts.getOrDefault(key, new AtomicInteger(0)).get();
    }
}
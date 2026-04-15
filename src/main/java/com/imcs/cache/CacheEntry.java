package com.imcs.cache;

public class CacheEntry {

    private final String key;
    private final String value;
    private final long expiryTime;
    private long lastAccessTime;
    private int accessCount;

    public CacheEntry(String key, String value, long ttl) {
        this.key = key;
        this.value = value;
        this.expiryTime = System.currentTimeMillis() + ttl;
        this.lastAccessTime = System.currentTimeMillis();
        this.accessCount = 0;
    }

    // Called every time entry is accessed via GET
    public void updateAccessMetadata() {
        this.accessCount++;
        this.lastAccessTime = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
    public long getExpiryTime() { return expiryTime; }
    public long getLastAccessTime() { return lastAccessTime; }
    public int getAccessCount() { return accessCount; }
}
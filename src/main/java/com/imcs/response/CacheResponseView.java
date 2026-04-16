package com.imcs.response;

public class CacheResponseView {

    private String key;
    private String value;
    private String status;
    private String message;

    public CacheResponseView() {}

    public CacheResponseView(String key, String value,
                              String status, String message) {
        this.key = key;
        this.value = value;
        this.status = status;
        this.message = message;
    }

    public static CacheResponseView hit(String key, String value) {
        return new CacheResponseView(key, value,
            "HIT", "Data retrieved from cache");
    }

    public static CacheResponseView miss(String key, String value) {
        return new CacheResponseView(key, value,
            "MISS", "Data retrieved from database");
    }

    public static CacheResponseView notFound(String key) {
        return new CacheResponseView(key, null,
            "NOT_FOUND", "Key not found in cache or database");
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
}
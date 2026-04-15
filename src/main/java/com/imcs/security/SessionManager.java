package com.imcs.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SessionManager {

    @Value("${security.session-timeout-ms:600000}")
    private long sessionTimeoutMs;

    private long lastActivityTime = System.currentTimeMillis();
    private Runnable onTimeout;

    public void recordActivity() {
        lastActivityTime = System.currentTimeMillis();
    }

    public boolean isSessionExpired() {
        return System.currentTimeMillis() - lastActivityTime > sessionTimeoutMs;
    }

    public long getRemainingMs() {
        long remaining = sessionTimeoutMs - (System.currentTimeMillis() - lastActivityTime);
        return Math.max(0, remaining);
    }

    public void setOnTimeout(Runnable callback) {
        this.onTimeout = callback;
    }

    public void triggerTimeout() {
        if (onTimeout != null) onTimeout.run();
    }

    public void reset() {
        lastActivityTime = System.currentTimeMillis();
    }
}
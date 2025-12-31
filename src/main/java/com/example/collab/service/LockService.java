package com.example.collab.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LockService {

    public static class LockInfo {
        private final String owner;
        private final Instant acquiredAt;
        private final Instant expiresAt;

        public LockInfo(String owner, Instant acquiredAt, Instant expiresAt) {
            this.owner = owner;
            this.acquiredAt = acquiredAt;
            this.expiresAt = expiresAt;
        }

        public String getOwner() { return owner; }
        public Instant getAcquiredAt() { return acquiredAt; }
        public Instant getExpiresAt() { return expiresAt; }

        public boolean isExpired() {
            return expiresAt != null && Instant.now().isAfter(expiresAt);
        }
    }

    private final Map<String, LockInfo> locks = new ConcurrentHashMap<>();
    private final long defaultTtlSeconds = 300; // 5 minutes

    private String key(String modelId, String featureId) {
        return modelId + "::" + featureId;
    }

    public Optional<LockInfo> getLock(String modelId, String featureId) {
        LockInfo info = locks.get(key(modelId, featureId));
        if (info == null) return Optional.empty();
        if (info.isExpired()) {
            locks.remove(key(modelId, featureId));
            return Optional.empty();
        }
        return Optional.of(info);
    }

    public boolean acquireLock(String modelId, String featureId, String username, Long ttlSeconds, boolean force) {
        String k = key(modelId, featureId);
        long ttl = ttlSeconds == null ? defaultTtlSeconds : ttlSeconds;
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttl);

        return locks.compute(k, (key, existing) -> {
            if (existing == null || existing.isExpired() || force || existing.getOwner().equals(username)) {
                return new LockInfo(username, now, exp);
            }
            // otherwise keep existing
            return existing;
        }).getOwner().equals(username);
    }

    public boolean releaseLock(String modelId, String featureId, String username, boolean force) {
        String k = key(modelId, featureId);
        LockInfo current = locks.get(k);
        if (current == null || current.isExpired()) {
            locks.remove(k);
            return true; // nothing to do
        }
        if (force || current.getOwner().equals(username)) {
            locks.remove(k);
            return true;
        }
        return false; // cannot release
    }
}
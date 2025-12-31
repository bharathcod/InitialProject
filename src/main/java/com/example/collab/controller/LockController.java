package com.example.collab.controller;

import com.example.collab.service.LockService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/models/{modelId}/features/{featureId}")
public class LockController {

    private final LockService lockService;
    private final com.example.collab.messaging.RedisPublisher redisPublisher;

    public LockController(LockService lockService, com.example.collab.messaging.RedisPublisher redisPublisher) {
        this.lockService = lockService;
        this.redisPublisher = redisPublisher;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/lock")
    public ResponseEntity<Map<String, Object>> lockFeature(@PathVariable String modelId,
                                                           @PathVariable String featureId,
                                                           @RequestBody(required = false) Map<String, Object> body,
                                                           Authentication auth) {
        String user = auth == null ? null : auth.getName();
        Long ttl = body != null && body.containsKey("ttlSeconds") ? ((Number) body.get("ttlSeconds")).longValue() : null;
        boolean force = body != null && Boolean.TRUE.equals(body.get("force"));
        boolean ok = lockService.acquireLock(modelId, featureId, user, ttl, force);
        if (!ok) {
            return ResponseEntity.status(409).body(Map.of("status", "LOCKED"));
        }
        // publish lock event
        String level = body != null && body.containsKey("level") ? String.valueOf(body.get("level")) : "FEATURE";
        var ev = new com.example.collab.dto.LockEvent(modelId, featureId, level, "LOCK", user, java.time.Instant.now());
        redisPublisher.publishLock(ev);
        var lockInfo = lockService.getLock(modelId, featureId).get();
        return ResponseEntity.ok(Map.of("owner", lockInfo.getOwner(), "acquiredAt", lockInfo.getAcquiredAt(), "expiresAt", lockInfo.getExpiresAt()));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/unlock")
    public ResponseEntity<?> unlockFeature(@PathVariable String modelId,
                                           @PathVariable String featureId,
                                           @RequestBody(required = false) Map<String, Object> body,
                                           Authentication auth) {
        String user = auth == null ? null : auth.getName();
        boolean force = body != null && Boolean.TRUE.equals(body.get("force"));
        boolean ok = lockService.releaseLock(modelId, featureId, user, force);
        if (!ok) {
            return ResponseEntity.status(403).body(Map.of("status", "FORBIDDEN"));
        }
        String level = body != null && body.containsKey("level") ? String.valueOf(body.get("level")) : "FEATURE";
        var ev = new com.example.collab.dto.LockEvent(modelId, featureId, level, "UNLOCK", user, java.time.Instant.now());
        redisPublisher.publishLock(ev);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/lock")
    public ResponseEntity<?> getLock(@PathVariable String modelId,
                                     @PathVariable String featureId) {
        var opt = lockService.getLock(modelId, featureId);
        return opt.map(li -> ResponseEntity.ok(Map.of("owner", li.getOwner(), "acquiredAt", li.getAcquiredAt(), "expiresAt", li.getExpiresAt())))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
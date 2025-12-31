package com.example.collab.controller;

import com.example.collab.dto.ChangeEvent;
import com.example.collab.service.CollaborationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ModelWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(ModelWebSocketController.class);

    private final CollaborationService collaborationService;
    private final com.example.collab.messaging.RedisPublisher redisPublisher;
    private final com.example.collab.service.LockService lockService;

    public ModelWebSocketController(CollaborationService collaborationService, com.example.collab.messaging.RedisPublisher redisPublisher, com.example.collab.service.LockService lockService) {
        this.collaborationService = collaborationService;
        this.redisPublisher = redisPublisher;
        this.lockService = lockService;
    }

    @MessageMapping("/update-model") // clients send to /app/update-model
    public void handleModelUpdate(ChangeEvent event) {
        log.debug("Received update-model event: {}", event);
        // Optionally validate event fields
        collaborationService.processChange(event);
        // We don't directly send to clients here — publisher -> Redis -> subscriber -> broadcast
    }

    @MessageMapping("/model/lock") // clients send to /app/model/lock
    public void handleLock(com.example.collab.dto.LockEvent lockEvent, org.springframework.messaging.simp.SimpMessageHeaderAccessor headers) {
        if (lockEvent.getTimestamp() == null) lockEvent.setTimestamp(java.time.Instant.now());
        // Try to acquire/release via LockService
        try {
            String action = lockEvent.getAction();
            String user = lockEvent.getUser();
            if ("LOCK".equalsIgnoreCase(action)) {
                boolean ok = lockService.acquireLock(lockEvent.getModelId(), lockEvent.getFeatureId(), user, null, false);
                if (ok) redisPublisher.publishLock(lockEvent);
            } else if ("UNLOCK".equalsIgnoreCase(action)) {
                boolean ok = lockService.releaseLock(lockEvent.getModelId(), lockEvent.getFeatureId(), user, false);
                if (ok) redisPublisher.publishLock(lockEvent);
            }
        } catch (Exception e) {
            log.warn("Lock handling failed", e);
        }
    }

    @MessageMapping("/file/add") // clients send to /app/file/add
    public void handleFileAdd(com.example.collab.dto.FileAddEvent fileAddEvent) {
        if (fileAddEvent.getTimestamp() == null) fileAddEvent.setTimestamp(java.time.Instant.now());
        // persist as a ModelOperation? For now publish to Redis so clients can react
        redisPublisher.publishFileAdd(fileAddEvent);
    }

    @MessageMapping("/presence") // clients send presence events (JOIN/LEAVE)
    public void handlePresence(com.example.collab.dto.PresenceEvent presenceEvent) {
        if (presenceEvent.getTimestamp() == null) presenceEvent.setTimestamp(java.time.Instant.now());
        redisPublisher.publishPresence(presenceEvent);
    }
}

package com.example.collab.service;

import com.example.collab.dto.ChangeEvent;
import com.example.collab.messaging.RedisPublisher;
import com.example.collab.persistence.entity.ModelOperation;
import com.example.collab.persistence.repository.ModelOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CollaborationService {

    private static final Logger log = LoggerFactory.getLogger(CollaborationService.class);

    private final ModelOperationRepository repo;
    private final RedisPublisher publisher;
    private final SimpMessagingTemplate wsTemplate;
    private final com.example.collab.util.InstanceId instanceId;
    private final com.example.collab.service.LockService lockService;

    public CollaborationService(ModelOperationRepository repo, RedisPublisher publisher, SimpMessagingTemplate wsTemplate, com.example.collab.util.InstanceId instanceId, com.example.collab.service.LockService lockService) {
        this.repo = repo;
        this.publisher = publisher;
        this.wsTemplate = wsTemplate;
        this.instanceId = instanceId;
        this.lockService = lockService;
    }

    @Transactional
    public void processChange(ChangeEvent event) {
        log.debug("Processing change for model {} feature {}", event.getModelId(), event.getFeatureId());
        // Ensure feature lock (if present) is owned by the author
        var lockOpt = lockService.getLock(event.getModelId(), event.getFeatureId());
        if (lockOpt.isPresent()) {
            var li = lockOpt.get();
            if (!li.getOwner().equals(event.getAuthor())) {
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Feature is locked by " + li.getOwner());
            }
        }

        // Persist change (so we have durable audit)
        ModelOperation op = new ModelOperation();
        op.setModelId(event.getModelId());
        op.setFeatureId(event.getFeatureId());
        op.setChangeType(event.getChangeType());
        op.setPayload(event.getPayload());
        op.setAuthor(event.getAuthor());
        op.setTimestamp(event.getTimestamp() == null ? Instant.now() : event.getTimestamp());
        repo.save(op);
        log.debug("Saved ModelOperation id={} for model={}", op.getId(), op.getModelId());

        // Publish to Redis so other instances can broadcast to their clients
        try {
            // mark origin so subscribers can ignore the message on this instance
            event.setOriginId(instanceId.getId());
            publisher.publish(event);
            log.debug("Published change to Redis channel for model {} (origin={})", event.getModelId(), event.getOriginId());
        } catch (Exception e) {
            log.warn("Failed to publish to Redis, will still attempt local broadcast", e);
        }

        // Also send locally to a model-specific topic so single-node setups get immediate updates
        try {
            wsTemplate.convertAndSend("/topic/model-updates/" + event.getModelId(), event);
            log.debug("Sent local WS broadcast for model {}", event.getModelId());
        } catch (Exception ex) {
            log.error("Failed to broadcast locally for model {}", event.getModelId(), ex);
        }
    }
}

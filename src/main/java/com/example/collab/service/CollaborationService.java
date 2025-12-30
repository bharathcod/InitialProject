package com.example.collab.service;

import com.example.collab.dto.ChangeEvent;
import com.example.collab.messaging.RedisPublisher;
import com.example.collab.persistence.entity.ModelOperation;
import com.example.collab.persistence.repository.ModelOperationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CollaborationService {

    private final ModelOperationRepository repo;
    private final RedisPublisher publisher;

    public CollaborationService(ModelOperationRepository repo, RedisPublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

    @Transactional
    public void processChange(ChangeEvent event) {
        // Persist change (so we have durable audit)
        ModelOperation op = new ModelOperation();
        op.setModelId(event.getModelId());
        op.setFeatureId(event.getFeatureId());
        op.setChangeType(event.getChangeType());
        op.setPayload(event.getPayload());
        op.setAuthor(event.getAuthor());
        op.setTimestamp(event.getTimestamp() == null ? Instant.now() : event.getTimestamp());
        repo.save(op);

        // Publish to Redis so other instances can broadcast to their clients
        publisher.publish(event);
    }
}

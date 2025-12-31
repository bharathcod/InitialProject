package com.example.collab.service;

import com.example.collab.dto.ChangeEvent;
import com.example.collab.persistence.entity.ModelOperation;
import com.example.collab.persistence.repository.ModelOperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CollaborationServiceLockTest {

    private ModelOperationRepository repo;
    private com.example.collab.messaging.RedisPublisher publisher;
    private org.springframework.messaging.simp.SimpMessagingTemplate wsTemplate;
    private com.example.collab.util.InstanceId instanceId;
    private LockService lockService;
    private CollaborationService service;

    @BeforeEach
    void setUp() {
        repo = mock(ModelOperationRepository.class);
        publisher = mock(com.example.collab.messaging.RedisPublisher.class);
        wsTemplate = mock(org.springframework.messaging.simp.SimpMessagingTemplate.class);
        instanceId = new com.example.collab.util.InstanceId();
        lockService = new LockService();
        service = new CollaborationService(repo, publisher, wsTemplate, instanceId, lockService);
    }

    @Test
    void changeForbiddenIfLockedByOther() {
        // lock by bob
        lockService.acquireLock("M-1","F-1","bob", 60L, false);
        ChangeEvent event = new ChangeEvent();
        event.setModelId("M-1");
        event.setFeatureId("F-1");
        event.setChangeType("UPDATE");
        event.setAuthor("alice");
        event.setPayload("{}");
        event.setTimestamp(Instant.now());

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> service.processChange(event));
    }
}
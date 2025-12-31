package com.example.collab.service;

import com.example.collab.dto.ChangeEvent;
import com.example.collab.messaging.RedisPublisher;
import com.example.collab.persistence.entity.ModelOperation;
import com.example.collab.persistence.repository.ModelOperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CollaborationServiceTest {

    private ModelOperationRepository repo;
    private RedisPublisher publisher;
    private SimpMessagingTemplate wsTemplate;
    private CollaborationService service;

    @BeforeEach
    void setUp() {
        repo = mock(ModelOperationRepository.class);
        publisher = mock(RedisPublisher.class);
        wsTemplate = mock(SimpMessagingTemplate.class);
        com.example.collab.util.InstanceId instanceId = new com.example.collab.util.InstanceId();
        service = new CollaborationService(repo, publisher, wsTemplate, instanceId);
    }

    @Test
    void processChange_persistsAndPublishesAndSendsLocally() {
        ChangeEvent event = new ChangeEvent();
        event.setModelId("model-1");
        event.setFeatureId("feature-1");
        event.setChangeType("UPDATE");
        event.setPayload("{\"foo\":\"bar\"}");
        event.setAuthor("alice");
        event.setTimestamp(Instant.now());

        when(repo.save(any(ModelOperation.class))).thenAnswer(inv -> inv.getArgument(0));

        service.processChange(event);

        // verify persisted
        ArgumentCaptor<ModelOperation> opCaptor = ArgumentCaptor.forClass(ModelOperation.class);
        verify(repo, times(1)).save(opCaptor.capture());
        ModelOperation saved = opCaptor.getValue();
        assertEquals("model-1", saved.getModelId());
        assertEquals("feature-1", saved.getFeatureId());
        assertEquals("UPDATE", saved.getChangeType());
        assertEquals("{\"foo\":\"bar\"}", saved.getPayload());
        assertEquals("alice", saved.getAuthor());
        assertNotNull(saved.getTimestamp());

        // verify redis publisher invoked
        verify(publisher, times(1)).publish(event);

        // verify local websocket broadcast invoked on per-model topic
        verify(wsTemplate, times(1)).convertAndSend(eq("/topic/model-updates/" + event.getModelId()), eq(event));
    }
}
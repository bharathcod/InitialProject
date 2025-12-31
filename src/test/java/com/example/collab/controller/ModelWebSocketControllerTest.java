package com.example.collab.controller;

import com.example.collab.dto.FileAddEvent;
import com.example.collab.dto.LockEvent;
import com.example.collab.dto.PresenceEvent;
import com.example.collab.messaging.RedisPublisher;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.*;

public class ModelWebSocketControllerTest {

    @Test
    public void lockEvent_publishes() {
        var collab = mock(com.example.collab.service.CollaborationService.class);
        var pub = mock(RedisPublisher.class);
        var ctrl = new ModelWebSocketController(collab, pub);

        var lock = new LockEvent("M-1", "F-1", "FEATURE", "LOCK", "alice", null);
        ctrl.handleLock(lock);
        verify(pub, times(1)).publishLock(any());
    }

    @Test
    public void fileAdd_publishes() {
        var collab = mock(com.example.collab.service.CollaborationService.class);
        var pub = mock(RedisPublisher.class);
        var ctrl = new ModelWebSocketController(collab, pub);

        var fa = new FileAddEvent("M-1", "file-1", "notes.txt", "alice", null);
        ctrl.handleFileAdd(fa);
        verify(pub, times(1)).publishFileAdd(any());
    }

    @Test
    public void presence_publishes() {
        var collab = mock(com.example.collab.service.CollaborationService.class);
        var pub = mock(RedisPublisher.class);
        var ctrl = new ModelWebSocketController(collab, pub);

        var pe = new PresenceEvent(1L, "alice", "JOIN", Instant.now());
        ctrl.handlePresence(pe);
        verify(pub, times(1)).publishPresence(any());
    }
}

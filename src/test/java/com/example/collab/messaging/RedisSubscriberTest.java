package com.example.collab.messaging;

import com.example.collab.config.RedisConfig;
import com.example.collab.dto.ChangeEvent;
import com.example.collab.dto.FileAddEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;

import static org.mockito.Mockito.*;

class RedisSubscriberTest {

    private SimpMessagingTemplate wsTemplate;
    private RedisSubscriber subscriber;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        wsTemplate = mock(SimpMessagingTemplate.class);
        // Configure ObjectMapper to handle Java Time types (Instant)
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // use a fixed instance id for deterministic tests
        com.example.collab.util.InstanceId instanceId = new com.example.collab.util.InstanceId();
        subscriber = new RedisSubscriber(wsTemplate, objectMapper, instanceId);
    }

    @Test
    void onMessage_forChangeEvent_sendsToPerModelTopic() throws Exception {
        ChangeEvent event = new ChangeEvent();
        event.setModelId("M-42");
        event.setFeatureId("F-1");
        event.setChangeType("UPDATE");
        event.setAuthor("alice");
        event.setTimestamp(Instant.now());

        String payload = objectMapper.writeValueAsString(event);
        Message message = new org.springframework.data.redis.connection.DefaultMessage(payload.getBytes(), RedisConfig.CHANNEL.getBytes());

        subscriber.onMessage(message, null);

        verify(wsTemplate, times(1)).convertAndSend(eq("/topic/model-updates/M-42"), any(ChangeEvent.class));
    }

    @Test
    void onMessage_forChangeEvent_withSameOrigin_skipsBroadcast() throws Exception {
        // create an instance id and embed it in the event
        com.example.collab.util.InstanceId instanceId = new com.example.collab.util.InstanceId();
        // create a subscriber bound to this instance id
        RedisSubscriber localSub = new RedisSubscriber(wsTemplate, objectMapper, instanceId);

        ChangeEvent event = new ChangeEvent();
        event.setModelId("M-99");
        event.setFeatureId("F-9");
        event.setChangeType("UPDATE");
        event.setAuthor("bob");
        event.setTimestamp(Instant.now());
        event.setOriginId(instanceId.getId());

        String payload = objectMapper.writeValueAsString(event);
        Message message = new org.springframework.data.redis.connection.DefaultMessage(payload.getBytes(), RedisConfig.CHANNEL.getBytes());

        localSub.onMessage(message, null);

        // Because origin matches instance id, subscriber should NOT broadcast
        verify(wsTemplate, never()).convertAndSend(eq("/topic/model-updates/M-99"), any(ChangeEvent.class));
    }
    @Test
    void onMessage_forFileAddEvent_sendsToPerModelFileTopics() throws Exception {
        FileAddEvent fa = new FileAddEvent("M-7", "file-1", "doc.txt", "bob", Instant.now());
        String payload = objectMapper.writeValueAsString(fa);
        Message message = new org.springframework.data.redis.connection.DefaultMessage(payload.getBytes(), RedisConfig.FILES_CHANNEL.getBytes());

        subscriber.onMessage(message, null);

        verify(wsTemplate, times(1)).convertAndSend(eq("/topic/model-updates/M-7"), any(FileAddEvent.class));
        verify(wsTemplate, times(1)).convertAndSend(eq("/topic/files/M-7"), any(FileAddEvent.class));
    }
}
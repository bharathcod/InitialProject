package com.example.collab.messaging;

import com.example.collab.config.RedisConfig;
import com.example.collab.dto.ChangeEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;

import static org.mockito.Mockito.*;

class RedisPublisherJsr310Test {

    private StringRedisTemplate redisTemplate;
    private ObjectMapper objectMapper;
    private RedisPublisher publisher;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        publisher = new RedisPublisher(redisTemplate, objectMapper);
    }

    @Test
    void publish_changeEvent_withInstant_serializesAndSends() {
        ChangeEvent event = new ChangeEvent();
        event.setModelId("M-100");
        event.setFeatureId("F-100");
        event.setChangeType("UPDATE");
        event.setAuthor("alice");
        event.setTimestamp(Instant.now());
        event.setPayload("{\"x\":1}");

        // No exception should be thrown during serialization
        publisher.publish(event);

        // verify redisTemplate was invoked with the correct channel
        verify(redisTemplate, times(1)).convertAndSend(eq(RedisConfig.CHANNEL), anyString());
    }
}
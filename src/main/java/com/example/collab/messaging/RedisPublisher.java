package com.example.collab.messaging;

import com.example.collab.config.RedisConfig;
import com.example.collab.dto.ChangeEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisPublisher(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(ChangeEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(RedisConfig.CHANNEL, payload);
        } catch (Exception e) {
            // handle error/logging
            e.printStackTrace();
        }
    }

    // Publish a permission notification (so other instances can broadcast it)
    public void publishPermission(com.example.collab.dto.PermissionNotification notification) {
        try {
            String payload = objectMapper.writeValueAsString(notification);
            redisTemplate.convertAndSend(RedisConfig.PERMISSIONS_CHANNEL, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Publish a lock event
    public void publishLock(com.example.collab.dto.LockEvent lockEvent) {
        try {
            String payload = objectMapper.writeValueAsString(lockEvent);
            redisTemplate.convertAndSend(RedisConfig.LOCKS_CHANNEL, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Publish a file add event
    public void publishFileAdd(com.example.collab.dto.FileAddEvent fileAddEvent) {
        try {
            String payload = objectMapper.writeValueAsString(fileAddEvent);
            redisTemplate.convertAndSend(RedisConfig.FILES_CHANNEL, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Publish presence events
    public void publishPresence(com.example.collab.dto.PresenceEvent presenceEvent) {
        try {
            String payload = objectMapper.writeValueAsString(presenceEvent);
            redisTemplate.convertAndSend(RedisConfig.PRESENCE_CHANNEL, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

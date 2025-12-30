package com.example.collab.messaging;

import com.example.collab.config.RedisConfig;
import com.example.collab.dto.ChangeEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RedisPublisher(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
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
}

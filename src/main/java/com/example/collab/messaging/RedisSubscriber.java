package com.example.collab.messaging;

import com.example.collab.dto.ChangeEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate wsTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RedisSubscriber(SimpMessagingTemplate wsTemplate) {
        this.wsTemplate = wsTemplate;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody());
            ChangeEvent event = objectMapper.readValue(payload, ChangeEvent.class);
            // Broadcast to all clients subscribed to /topic/model-updates
            wsTemplate.convertAndSend("/topic/model-updates", event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

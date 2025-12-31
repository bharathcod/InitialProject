package com.example.collab.messaging;

import com.example.collab.config.RedisConfig;
import com.example.collab.dto.ChangeEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisSubscriber implements MessageListener {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RedisSubscriber.class);

    private final SimpMessagingTemplate wsTemplate;
    private final ObjectMapper objectMapper;
    private final com.example.collab.util.InstanceId instanceId;

    public RedisSubscriber(SimpMessagingTemplate wsTemplate, ObjectMapper objectMapper, com.example.collab.util.InstanceId instanceId) {
        this.wsTemplate = wsTemplate;
        this.objectMapper = objectMapper;
        this.instanceId = instanceId;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody());
            String channel = new String(message.getChannel());
            log.debug("RedisSubscriber received message on channel {}: {}", channel, payload);
            if (RedisConfig.CHANNEL.equals(channel)) {
                ChangeEvent event = objectMapper.readValue(payload, ChangeEvent.class);
                // If this event originated from this instance, skip re-broadcast to avoid duplicates
                if (event.getOriginId() != null && event.getOriginId().equals(instanceId.getId())) {
                    log.debug("Skipping ChangeEvent for model {} because originId matches this instance ({})", event.getModelId(), instanceId.getId());
                } else {
                    // Broadcast to model-specific topic so clients only receive relevant updates
                    String dest = "/topic/model-updates/" + event.getModelId();
                    wsTemplate.convertAndSend(dest, event);
                    log.debug("Forwarded ChangeEvent for model {} to {}", event.getModelId(), dest);
                }
            } else if (RedisConfig.PERMISSIONS_CHANNEL.equals(channel)) {
                // Permission notifications
                com.example.collab.dto.PermissionNotification notif = objectMapper.readValue(payload, com.example.collab.dto.PermissionNotification.class);
                wsTemplate.convertAndSend("/topic/permissions", notif);
            } else if (RedisConfig.LOCKS_CHANNEL.equals(channel)) {
                com.example.collab.dto.LockEvent lock = objectMapper.readValue(payload, com.example.collab.dto.LockEvent.class);
                wsTemplate.convertAndSend("/topic/locks", lock);
            } else if (RedisConfig.PRESENCE_CHANNEL.equals(channel)) {
                com.example.collab.dto.PresenceEvent pe = objectMapper.readValue(payload, com.example.collab.dto.PresenceEvent.class);
                wsTemplate.convertAndSend("/topic/presence", pe);
            } else if (RedisConfig.FILES_CHANNEL.equals(channel)) {
                com.example.collab.dto.FileAddEvent fa = objectMapper.readValue(payload, com.example.collab.dto.FileAddEvent.class);
                // Broadcast file-add events to model-specific topic and files topic
                String dest1 = "/topic/model-updates/" + fa.getModelId();
                String dest2 = "/topic/files/" + fa.getModelId();
                wsTemplate.convertAndSend(dest1, fa);
                wsTemplate.convertAndSend(dest2, fa);
                log.debug("Forwarded FileAddEvent for model {} to {} and {}", fa.getModelId(), dest1, dest2);
            } else {
                // unknown channel - ignore or log
                log.warn("Received message on unknown channel: {}", channel);
            }
        } catch (Exception e) {
            log.error("Error handling Redis message", e);
        }
    }
}

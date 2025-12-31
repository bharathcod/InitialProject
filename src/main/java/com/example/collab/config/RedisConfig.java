package com.example.collab.config;

import com.example.collab.messaging.RedisSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {

    public static final String CHANNEL = "model-updates";
    public static final String PERMISSIONS_CHANNEL = "permissions";
    public static final String LOCKS_CHANNEL = "locks";
    public static final String PRESENCE_CHANNEL = "presence";
    public static final String FILES_CHANNEL = "files";

    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory,
                                                        MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic(CHANNEL));
        // listen for permission events as well
        container.addMessageListener(listenerAdapter, new PatternTopic(PERMISSIONS_CHANNEL));
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
        // The "onMessage" method will be called when a message arrives
        return new MessageListenerAdapter(subscriber);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}

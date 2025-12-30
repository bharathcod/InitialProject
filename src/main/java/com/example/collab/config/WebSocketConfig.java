package com.example.collab.config;

import com.example.collab.security.JwtHandshakeInterceptor;
import com.example.collab.security.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;

    public WebSocketConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(new JwtHandshakeInterceptor(jwtUtil))
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(org.springframework.http.server.ServerHttpRequest request,
                                                      org.springframework.web.socket.WebSocketHandler wsHandler,
                                                      Map<String, Object> attributes) {
                        String username = (String) attributes.get("username");
                        if (username != null) {
                            return new JwtHandshakeInterceptor.StompPrincipal(username);
                        }
                        return super.determineUser(request, wsHandler, attributes);
                    }
                })
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }
}

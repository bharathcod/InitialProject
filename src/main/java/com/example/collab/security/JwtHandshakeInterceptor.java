package com.example.collab.security;

import org.springframework.http.server.*;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    public JwtHandshakeInterceptor(JwtUtil jwtUtil) { this.jwtUtil = jwtUtil; }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   org.springframework.web.socket.WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        // Prefer Authorization header
        var headers = request.getHeaders();
        String token = null;
        if (headers.containsKey("Authorization")) {
            var auth = headers.getFirst("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) token = auth.substring(7);
        }

        // fallback: query param ?access_token=...
        if (token == null && request instanceof ServletServerHttpRequest servletRequest) {
            var param = servletRequest.getServletRequest().getParameter("access_token");
            if (param != null) token = param;
        }

        if (token == null || !jwtUtil.validateToken(token)) {
            // reject handshake
            return false;
        }

        String username = jwtUtil.getUsername(token);

        // attach principal to attributes so handshake handler can create a Principal
        attributes.put("username", username);
        attributes.put("roles", jwtUtil.getRoles(token));

        return true; // allow handshake
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               org.springframework.web.socket.WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }

    // Small Principal wrapper
    public static class StompPrincipal implements Principal {
        private final String name;
        public StompPrincipal(String name) { this.name = name; }
        @Override public String getName() { return name; }
    }
}

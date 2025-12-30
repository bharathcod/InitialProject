package com.example.collab.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final Key signingKey;
    private final long validityMillis;

    public JwtUtil(@Value("${security.jwt.secret:change_this_secret_to_env}") String secret,
                   @Value("${security.jwt.expiration-ms:3600000}") long validityMillis) {
        byte[] keyBytes = Arrays.copyOf(secret.getBytes(), 64);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.validityMillis = validityMillis;
    }

    public String generateToken(String username, Collection<String> roles) {
        long now = System.currentTimeMillis();
        Date iat = new Date(now);
        Date exp = new Date(now + validityMillis);

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(iat)
                .setExpiration(exp)
                .addClaims(claims)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(signingKey).build()
                .parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(signingKey).build()
                .parseClaimsJws(token).getBody();
        Object roles = claims.get("roles");
        if (roles instanceof List) {
            return ((List<?>) roles).stream().map(Object::toString).collect(Collectors.toList());
        }
        return List.of();
    }
}

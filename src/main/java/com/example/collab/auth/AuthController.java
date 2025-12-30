package com.example.collab.auth;

import com.example.collab.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authManager,
            JwtUtil jwtUtil) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
    }

    record LoginRequest(String username, String password) {
    }

    record TokenResponse(String token) {
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            var ud = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
            var roles = ud.getAuthorities().stream().map(a -> a.getAuthority().replace("ROLE_", ""))
                    .collect(Collectors.toList());
            String token = jwtUtil.generateToken(ud.getUsername(), roles);
            return ResponseEntity.ok(new TokenResponse(token));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body("Invalid username/password");
        }
    }
}

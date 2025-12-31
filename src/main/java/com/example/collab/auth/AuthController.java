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
@org.springframework.web.bind.annotation.CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authManager,
            JwtUtil jwtUtil, UserRepository userRepository, org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    record LoginRequest(String username, String password) {
    }

    record TokenResponse(String token) {
    }

    record RegisterRequest(String username, String password, java.util.Set<String> roles) {}

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

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req == null || req.username() == null || req.password() == null) {
            return ResponseEntity.badRequest().body("username and password required");
        }
        if (userRepository.existsById(req.username())) {
            return ResponseEntity.status(409).body("User exists");
        }
        var roles = req.roles() == null || req.roles().isEmpty() ? java.util.Set.of("VIEWER") : req.roles();
        UserEntity ue = new UserEntity(req.username(), passwordEncoder.encode(req.password()), roles);
        userRepository.save(ue);
        return ResponseEntity.status(201).body("Created");
    }
}

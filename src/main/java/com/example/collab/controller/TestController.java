package com.example.collab.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/test/whoami")
    public Map<String, Object> whoami(Authentication auth) {
        if (auth == null)
            return Map.of("authenticated", false);
        return Map.of(
                "authenticated", auth.isAuthenticated(),
                "principal", auth.getName(),
                "authorities", auth.getAuthorities().stream().map(Object::toString).toList());
    }
}

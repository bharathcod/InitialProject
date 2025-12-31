package com.example.collab.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InstanceId {
    private final String id = UUID.randomUUID().toString();

    public String getId() {
        return id;
    }
}
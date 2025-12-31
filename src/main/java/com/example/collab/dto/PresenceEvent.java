package com.example.collab.dto;

import java.time.Instant;

public class PresenceEvent {
    private Long projectId;
    private String username;
    private String status; // JOIN or LEAVE
    private Instant timestamp;

    public PresenceEvent() {}

    public PresenceEvent(Long projectId, String username, String status, Instant timestamp) {
        this.projectId = projectId;
        this.username = username;
        this.status = status;
        this.timestamp = timestamp;
    }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}

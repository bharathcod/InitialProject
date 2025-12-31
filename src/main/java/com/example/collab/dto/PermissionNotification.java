package com.example.collab.dto;

import java.time.Instant;

public class PermissionNotification {
    private Long projectId;
    private String username;
    private String role;
    private String action; // SHARED, UPDATED, REMOVED
    private String performedBy;
    private Instant timestamp;

    public PermissionNotification() {}

    public PermissionNotification(Long projectId, String username, String role, String action, String performedBy, Instant timestamp) {
        this.projectId = projectId;
        this.username = username;
        this.role = role;
        this.action = action;
        this.performedBy = performedBy;
        this.timestamp = timestamp;
    }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}

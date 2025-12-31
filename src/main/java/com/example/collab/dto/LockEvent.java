package com.example.collab.dto;

import java.time.Instant;

public class LockEvent {
    private String modelId;
    private String featureId;
    private String level; // e.g., FEATURE, SKETCH
    private String action; // LOCK or UNLOCK
    private String user;
    private Instant timestamp;

    public LockEvent() {}

    public LockEvent(String modelId, String featureId, String level, String action, String user, Instant timestamp) {
        this.modelId = modelId;
        this.featureId = featureId;
        this.level = level;
        this.action = action;
        this.user = user;
        this.timestamp = timestamp;
    }

    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getFeatureId() { return featureId; }
    public void setFeatureId(String featureId) { this.featureId = featureId; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}

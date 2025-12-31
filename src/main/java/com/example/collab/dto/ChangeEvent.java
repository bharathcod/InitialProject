package com.example.collab.dto;

import java.time.Instant;

public class ChangeEvent {
    private String modelId;
    private String featureId;
    private String changeType; // e.g., ADD_FEATURE, UPDATE_DIM
    private String author;
    private Instant timestamp;
    private String payload; // free-form JSON with details
    private String originId; // instance id of the server that published this event

    public ChangeEvent() {}

    // getters/setters
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getFeatureId() { return featureId; }
    public void setFeatureId(String featureId) { this.featureId = featureId; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getOriginId() { return originId; }
    public void setOriginId(String originId) { this.originId = originId; }
}

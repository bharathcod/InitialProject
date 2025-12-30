package com.example.collab.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "model_operations")
public class ModelOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String modelId;
    private String featureId;
    private String changeType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private String author;
    private Instant timestamp;

    public ModelOperation() {}

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getFeatureId() { return featureId; }
    public void setFeatureId(String featureId) { this.featureId = featureId; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}

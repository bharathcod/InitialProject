package com.example.collab.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "features", indexes = {@Index(columnList = "feature_id, model_id")})
public class Feature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "feature_id", nullable = false)
    private String featureId;

    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String metadata;

    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", referencedColumnName = "model_id")
    private Model model;

    public Feature() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFeatureId() { return featureId; }
    public void setFeatureId(String featureId) { this.featureId = featureId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Model getModel() { return model; }
    public void setModel(Model model) { this.model = model; }
}
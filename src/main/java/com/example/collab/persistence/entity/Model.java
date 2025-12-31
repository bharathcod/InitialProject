package com.example.collab.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "models", indexes = {@Index(columnList = "model_id", unique = true)})
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_id", nullable = false, unique = true)
    private String modelId;

    private String name;

    private String description;

    private String owner;

    private Instant createdAt;

    public Model() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
package com.example.collab.dto;

import java.time.Instant;

public class ProjectDto {
    public static record ProjectCreateRequest(String name, String description, String modelId) {}
    public static class ProjectResponse {
        public Long id;
        public String name;
        public String description;
        public String modelId;
        public String owner;
        public Instant createdAt;

        public ProjectResponse() {}

        public ProjectResponse(Long id, String name, String description, String modelId, String owner, Instant createdAt) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.modelId = modelId;
            this.owner = owner;
            this.createdAt = createdAt;
        }
    }
}

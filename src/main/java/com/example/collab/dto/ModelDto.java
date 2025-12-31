package com.example.collab.dto;

import java.time.Instant;
import java.util.List;

public class ModelDto {
    public static record CreateRequest(String name, String description, String modelId, List<FeatureCreateRequest> features) {}
    public static record CreateResponse(String modelId, String name, String description, String owner, Instant createdAt) {}

    public static record FeatureCreateRequest(String featureId, String name, String description, String metadata) {}
    public static record FeatureResponse(String featureId, String name, String description, String metadata, Instant createdAt) {}

    public static record ModelWithFeatures(String modelId, String name, String description, String owner, Instant createdAt, List<FeatureResponse> features) {}
}
package com.example.collab.controller;

import com.example.collab.dto.ModelDto;
import com.example.collab.persistence.entity.Feature;
import com.example.collab.persistence.entity.Model;
import com.example.collab.service.ModelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/models")
public class ModelController {

    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ModelDto.ModelWithFeatures> createModel(@RequestBody ModelDto.CreateRequest req, Authentication auth) {
        Model m = modelService.createModel(req, auth == null ? "anonymous" : auth.getName());
        List<Feature> features = modelService.listFeatures(m.getModelId());
        var feResponses = features.stream().map(f -> new ModelDto.FeatureResponse(f.getFeatureId(), f.getName(), f.getDescription(), f.getMetadata(), f.getCreatedAt())).collect(Collectors.toList());
        ModelDto.ModelWithFeatures resp = new ModelDto.ModelWithFeatures(m.getModelId(), m.getName(), m.getDescription(), m.getOwner(), m.getCreatedAt(), feResponses);
        return ResponseEntity.created(URI.create("/models/" + m.getModelId())).body(resp);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{modelId}")
    public ResponseEntity<ModelDto.ModelWithFeatures> getModel(@PathVariable String modelId) {
        Model m = modelService.getModelByModelId(modelId);
        List<Feature> features = modelService.listFeatures(modelId);
        var feResponses = features.stream().map(f -> new ModelDto.FeatureResponse(f.getFeatureId(), f.getName(), f.getDescription(), f.getMetadata(), f.getCreatedAt())).collect(Collectors.toList());
        return ResponseEntity.ok(new ModelDto.ModelWithFeatures(m.getModelId(), m.getName(), m.getDescription(), m.getOwner(), m.getCreatedAt(), feResponses));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{modelId}/features")
    public ResponseEntity<ModelDto.FeatureResponse> addFeature(@PathVariable String modelId, @RequestBody ModelDto.FeatureCreateRequest req) {
        Feature f = modelService.addFeature(modelId, req);
        return ResponseEntity.created(URI.create("/models/" + modelId + "/features/" + f.getFeatureId())).body(new ModelDto.FeatureResponse(f.getFeatureId(), f.getName(), f.getDescription(), f.getMetadata(), f.getCreatedAt()));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{modelId}/features")
    public ResponseEntity<List<ModelDto.FeatureResponse>> listFeatures(@PathVariable String modelId) {
        List<Feature> features = modelService.listFeatures(modelId);
        List<ModelDto.FeatureResponse> resp = features.stream().map(f -> new ModelDto.FeatureResponse(f.getFeatureId(), f.getName(), f.getDescription(), f.getMetadata(), f.getCreatedAt())).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }
}
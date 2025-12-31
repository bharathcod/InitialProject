package com.example.collab.service;

import com.example.collab.dto.ModelDto;
import com.example.collab.persistence.entity.Feature;
import com.example.collab.persistence.entity.Model;
import com.example.collab.persistence.repository.FeatureRepository;
import com.example.collab.persistence.repository.ModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ModelService {

    private final ModelRepository modelRepository;
    private final FeatureRepository featureRepository;

    public ModelService(ModelRepository modelRepository, FeatureRepository featureRepository) {
        this.modelRepository = modelRepository;
        this.featureRepository = featureRepository;
    }

    public Model createModel(ModelDto.CreateRequest req, String owner) {
        Model m = new Model();
        m.setName(req.name());
        m.setDescription(req.description());
        m.setModelId(req.modelId() == null || req.modelId().isBlank() ? UUID.randomUUID().toString() : req.modelId());
        m.setOwner(owner);
        m.setCreatedAt(Instant.now());
        m = modelRepository.save(m);

        if (req.features() != null) {
            for (ModelDto.FeatureCreateRequest fr : req.features()) {
                Feature f = new Feature();
                f.setFeatureId(fr.featureId());
                f.setName(fr.name());
                f.setDescription(fr.description());
                f.setMetadata(fr.metadata());
                f.setCreatedAt(Instant.now());
                f.setModel(m);
                featureRepository.save(f);
            }
        }

        return m;
    }

    public Model getModelByModelId(String modelId) {
        return modelRepository.findByModelId(modelId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Model not found"));
    }

    public List<Feature> listFeatures(String modelId) {
        getModelByModelId(modelId);
        return featureRepository.findAllByModel_ModelIdOrderByIdAsc(modelId);
    }

    public Feature addFeature(String modelId, ModelDto.FeatureCreateRequest req) {
        Model m = getModelByModelId(modelId);
        Feature f = new Feature();
        f.setFeatureId(req.featureId());
        f.setName(req.name());
        f.setDescription(req.description());
        f.setMetadata(req.metadata());
        f.setCreatedAt(Instant.now());
        f.setModel(m);
        return featureRepository.save(f);
    }

    public Feature getFeature(String modelId, String featureId) {
        return featureRepository.findByModel_ModelIdAndFeatureId(modelId, featureId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Feature not found"));
    }
}
package com.example.collab.persistence.repository;

import com.example.collab.persistence.entity.Feature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeatureRepository extends JpaRepository<Feature, Long> {
    List<Feature> findAllByModel_ModelIdOrderByIdAsc(String modelId);
    java.util.Optional<Feature> findByModel_ModelIdAndFeatureId(String modelId, String featureId);
}
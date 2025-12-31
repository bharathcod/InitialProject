package com.example.collab.persistence.repository;

import com.example.collab.persistence.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModelRepository extends JpaRepository<Model, Long> {
    Optional<Model> findByModelId(String modelId);
}
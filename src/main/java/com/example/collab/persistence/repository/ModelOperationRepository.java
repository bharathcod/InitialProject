package com.example.collab.persistence.repository;

import com.example.collab.persistence.entity.ModelOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModelOperationRepository extends JpaRepository<ModelOperation, Long> {
    List<ModelOperation> findAllByModelIdOrderByTimestampAsc(String modelId);
}

package com.example.collab.persistence.repository;

import com.example.collab.persistence.entity.ProjectPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectPermissionRepository extends JpaRepository<ProjectPermission, Long> {
    List<ProjectPermission> findAllByProjectId(Long projectId);
    Optional<ProjectPermission> findByProjectIdAndUsername(Long projectId, String username);
    void deleteByProjectIdAndUsername(Long projectId, String username);
}

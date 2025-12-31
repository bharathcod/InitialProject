package com.example.collab.service;

import com.example.collab.dto.ProjectDto;
import com.example.collab.persistence.entity.Project;
import com.example.collab.persistence.repository.ProjectRepository;
import com.example.collab.persistence.repository.ModelOperationRepository;
import com.example.collab.persistence.entity.ModelOperation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ModelOperationRepository operationRepository;

    public ProjectService(ProjectRepository projectRepository, ModelOperationRepository operationRepository) {
        this.projectRepository = projectRepository;
        this.operationRepository = operationRepository;
    }

    public Project createProject(ProjectDto.ProjectCreateRequest req, String owner) {
        Project p = new Project();
        p.setName(req.name());
        p.setDescription(req.description());
        p.setModelId(req.modelId() == null || req.modelId().isBlank() ? UUID.randomUUID().toString() : req.modelId());
        p.setOwner(owner);
        p.setCreatedAt(Instant.now());
        return projectRepository.save(p);
    }

    public Project getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    public java.util.List<Project> listProjects() {
        return projectRepository.findAll();
    }

    public Map<String, Object> getModelInfo(Long projectId) {
        Project p = getProject(projectId);
        String modelId = p.getModelId();
        List<ModelOperation> ops = operationRepository.findAllByModelIdOrderByTimestampAsc(modelId);
        Map<String, Object> info = new HashMap<>();
        info.put("modelId", modelId);
        info.put("operationCount", ops.size());
        info.put("lastModified", ops.isEmpty() ? null : ops.get(ops.size()-1).getTimestamp());
        return info;
    }

    public List<ModelOperation> getModelOperations(Long projectId) {
        Project p = getProject(projectId);
        return operationRepository.findAllByModelIdOrderByTimestampAsc(p.getModelId());
    }
}

package com.example.collab.service;

import com.example.collab.auth.UserEntity;
import com.example.collab.auth.UserRepository;
import com.example.collab.persistence.entity.Project;
import com.example.collab.persistence.entity.ProjectPermission;
import com.example.collab.persistence.repository.ProjectPermissionRepository;
import com.example.collab.persistence.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectPermissionService {

    private final ProjectRepository projectRepository;
    private final ProjectPermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final com.example.collab.messaging.RedisPublisher redisPublisher;

    public ProjectPermissionService(ProjectRepository projectRepository,
                                    ProjectPermissionRepository permissionRepository,
                                    UserRepository userRepository,
                                    com.example.collab.messaging.RedisPublisher redisPublisher) {
        this.projectRepository = projectRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.redisPublisher = redisPublisher;
    }

    private Project getProjectOr404(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private void ensureIsOwner(Project project, String requester) {
        if (requester == null || !requester.equals(project.getOwner())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project owner can manage permissions");
        }
    }

    public ProjectPermission shareProject(Long projectId, String username, String role, String requester) {
        Project p = getProjectOr404(projectId);
        ensureIsOwner(p, requester);
        // verify user exists
        UserEntity user = userRepository.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ProjectPermission perm = permissionRepository.findByProjectIdAndUsername(projectId, username)
                .orElse(new ProjectPermission());
        perm.setProjectId(projectId);
        perm.setUsername(username);
        perm.setRole(role);
        ProjectPermission saved = permissionRepository.save(perm);
        // publish notification
        var notif = new com.example.collab.dto.PermissionNotification(projectId, username, role, "SHARED", requester, java.time.Instant.now());
        redisPublisher.publishPermission(notif);
        return saved;
    }

    public List<ProjectPermission> listPermissions(Long projectId, String requester) {
        Project p = getProjectOr404(projectId);
        ensureIsOwner(p, requester);
        return permissionRepository.findAllByProjectId(projectId);
    }

    public ProjectPermission updatePermission(Long projectId, String username, String role, String requester) {
        Project p = getProjectOr404(projectId);
        ensureIsOwner(p, requester);
        ProjectPermission perm = permissionRepository.findByProjectIdAndUsername(projectId, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found"));
        perm.setRole(role);
        ProjectPermission updated = permissionRepository.save(perm);
        var notif = new com.example.collab.dto.PermissionNotification(projectId, username, role, "UPDATED", requester, java.time.Instant.now());
        redisPublisher.publishPermission(notif);
        return updated;
    }

    public void removePermission(Long projectId, String username, String requester) {
        Project p = getProjectOr404(projectId);
        ensureIsOwner(p, requester);
        permissionRepository.findByProjectIdAndUsername(projectId, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found"));
        permissionRepository.deleteByProjectIdAndUsername(projectId, username);
        var notif = new com.example.collab.dto.PermissionNotification(projectId, username, null, "REMOVED", requester, java.time.Instant.now());
        redisPublisher.publishPermission(notif);
    }
}

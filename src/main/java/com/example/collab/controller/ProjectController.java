package com.example.collab.controller;

import com.example.collab.dto.ProjectDto;
import com.example.collab.dto.ProjectPermissionDto;
import com.example.collab.persistence.entity.ModelOperation;
import com.example.collab.persistence.entity.Project;
import com.example.collab.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final com.example.collab.service.ProjectPermissionService permissionService;

    public ProjectController(ProjectService projectService, com.example.collab.service.ProjectPermissionService permissionService) {
        this.projectService = projectService;
        this.permissionService = permissionService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ProjectDto.ProjectResponse> createProject(@RequestBody ProjectDto.ProjectCreateRequest req,
                                                                     Authentication auth) {
        Project p = projectService.createProject(req, auth == null ? "anonymous" : auth.getName());
        ProjectDto.ProjectResponse resp = new ProjectDto.ProjectResponse(p.getId(), p.getName(), p.getDescription(), p.getModelId(), p.getOwner(), p.getCreatedAt());
        return ResponseEntity.created(URI.create("/projects/" + p.getId())).body(resp);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto.ProjectResponse> getProject(@PathVariable Long id) {
        Project p = projectService.getProject(id);
        ProjectDto.ProjectResponse resp = new ProjectDto.ProjectResponse(p.getId(), p.getName(), p.getDescription(), p.getModelId(), p.getOwner(), p.getCreatedAt());
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/model")
    public ResponseEntity<Map<String, Object>> getModelInfo(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getModelInfo(id));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<java.util.List<ProjectDto.ProjectResponse>> listProjects() {
        var projects = projectService.listProjects().stream()
                .map(p -> new ProjectDto.ProjectResponse(p.getId(), p.getName(), p.getDescription(), p.getModelId(), p.getOwner(), p.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(projects);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/operations")
    public ResponseEntity<List<ModelOperation>> getModelOperations(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getModelOperations(id));
    }

    // --- Permissions / Sharing ---
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/share")
    public ResponseEntity<ProjectPermissionDto.PermissionResponse> shareProject(@PathVariable("id") Long projectId,
                                                                                  @RequestBody ProjectPermissionDto.ShareRequest req,
                                                                                  Authentication auth) {
        var perm = permissionService.shareProject(projectId, req.username(), req.role(), auth == null ? null : auth.getName());
        var resp = new ProjectPermissionDto.PermissionResponse(perm.getUsername(), perm.getRole());
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/permissions")
    public ResponseEntity<List<ProjectPermissionDto.PermissionResponse>> listPermissions(@PathVariable("id") Long projectId,
                                                                                           Authentication auth) {
        var perms = permissionService.listPermissions(projectId, auth == null ? null : auth.getName());
        var resp = perms.stream().map(p -> new ProjectPermissionDto.PermissionResponse(p.getUsername(), p.getRole())).toList();
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/permissions/{username}")
    public ResponseEntity<ProjectPermissionDto.PermissionResponse> updatePermission(@PathVariable("id") Long projectId,
                                                                                      @PathVariable("username") String username,
                                                                                      @RequestBody ProjectPermissionDto.UpdateRequest req,
                                                                                      Authentication auth) {
        var perm = permissionService.updatePermission(projectId, username, req.role(), auth == null ? null : auth.getName());
        var resp = new ProjectPermissionDto.PermissionResponse(perm.getUsername(), perm.getRole());
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/permissions/{username}")
    public ResponseEntity<?> removePermission(@PathVariable("id") Long projectId,
                                              @PathVariable("username") String username,
                                              Authentication auth) {
        permissionService.removePermission(projectId, username, auth == null ? null : auth.getName());
        return ResponseEntity.noContent().build();
    }
}

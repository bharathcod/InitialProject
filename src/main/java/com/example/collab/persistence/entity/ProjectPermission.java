package com.example.collab.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "project_permissions", uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "username"}))
public class ProjectPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id")
    private Long projectId;

    private String username;

    private String role; // e.g., VIEWER, EDITOR, OWNER

    public ProjectPermission() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

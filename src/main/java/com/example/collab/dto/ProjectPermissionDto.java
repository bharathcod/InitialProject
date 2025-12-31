package com.example.collab.dto;

public class ProjectPermissionDto {
    public static record ShareRequest(String username, String role) {}
    public static record UpdateRequest(String role) {}
    public static class PermissionResponse {
        public String username;
        public String role;
        public PermissionResponse() {}
        public PermissionResponse(String username, String role) { this.username = username; this.role = role; }
    }
}

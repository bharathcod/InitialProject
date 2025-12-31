package com.example.collab.controller;

import com.example.collab.dto.ProjectPermissionDto;
import com.example.collab.persistence.entity.ProjectPermission;
import com.example.collab.service.ProjectPermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
public class ProjectPermissionControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ProjectPermissionService permissionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(username = "alice")
    public void shareProject_returnsPermission() throws Exception {
        when(permissionService.shareProject(anyLong(), eq("bob"), eq("EDITOR"), eq("alice")))
                .thenReturn(createPerm("bob", "EDITOR"));

        var req = new ProjectPermissionDto.ShareRequest("bob", "EDITOR");
        mvc.perform(post("/projects/1/share").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"))
                .andExpect(jsonPath("$.role").value("EDITOR"));
    }

    @Test
    @WithMockUser(username = "alice")
    public void listPermissions_returnsList() throws Exception {
        when(permissionService.listPermissions(1L, "alice"))
                .thenReturn(List.of(createPerm("bob","EDITOR"), createPerm("charlie","VIEWER")));

        mvc.perform(get("/projects/1/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("bob"))
                .andExpect(jsonPath("$[1].role").value("VIEWER"));
    }

    @Test
    @WithMockUser(username = "alice")
    public void updatePermission_returnsUpdated() throws Exception {
        when(permissionService.updatePermission(1L, "bob", "OWNER", "alice"))
                .thenReturn(createPerm("bob", "OWNER"));

        var req = new ProjectPermissionDto.UpdateRequest("OWNER");
        mvc.perform(put("/projects/1/permissions/bob").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("OWNER"));
    }

    @Test
    @WithMockUser(username = "alice")
    public void deletePermission_returnsNoContent() throws Exception {
        mvc.perform(delete("/projects/1/permissions/bob"))
                .andExpect(status().isNoContent());
    }

    private ProjectPermission createPerm(String username, String role) {
        var p = new ProjectPermission();
        p.setProjectId(1L);
        p.setUsername(username);
        p.setRole(role);
        return p;
    }
}

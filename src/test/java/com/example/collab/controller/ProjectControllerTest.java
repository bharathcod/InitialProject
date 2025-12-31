package com.example.collab.controller;

import com.example.collab.dto.ProjectDto;
import com.example.collab.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
public class ProjectControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ProjectService projectService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(username = "alice")
    public void createProject_returnsCreated() throws Exception {
        ProjectDto.ProjectCreateRequest req = new ProjectDto.ProjectCreateRequest("P1", "desc", null);
        var saved = new com.example.collab.persistence.entity.Project();
        saved.setId(1L);
        saved.setName("P1");
        saved.setDescription("desc");
        saved.setModelId("M-1");
        saved.setOwner("alice");
        saved.setCreatedAt(Instant.now());

        when(projectService.createProject(any(), eq("alice"))).thenReturn(saved);

        mvc.perform(post("/projects").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/projects/1"))
                .andExpect(jsonPath("$.id").value(1));

        verify(projectService, times(1)).createProject(any(), eq("alice"));
    }

    @Test
    @WithMockUser
    public void getModelInfo_returnsMap() throws Exception {
        when(projectService.getModelInfo(1L)).thenReturn(Map.of("modelId", "M-1", "operationCount", 2));

        mvc.perform(get("/projects/1/model"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelId").value("M-1"))
                .andExpect(jsonPath("$.operationCount").value(2));
    }
}

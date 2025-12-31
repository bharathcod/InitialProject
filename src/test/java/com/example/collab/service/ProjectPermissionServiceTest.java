package com.example.collab.service;

import com.example.collab.persistence.entity.Project;
import com.example.collab.persistence.entity.ProjectPermission;
import com.example.collab.persistence.repository.ProjectPermissionRepository;
import com.example.collab.persistence.repository.ProjectRepository;
import com.example.collab.auth.UserRepository;
import com.example.collab.messaging.RedisPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProjectPermissionServiceTest {

    @Test
    public void shareProject_publishesNotification() {
        ProjectRepository projectRepo = mock(ProjectRepository.class);
        ProjectPermissionRepository permRepo = mock(ProjectPermissionRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        RedisPublisher redisPublisher = mock(RedisPublisher.class);

        var svc = new com.example.collab.service.ProjectPermissionService(projectRepo, permRepo, userRepo, redisPublisher);

        var project = new Project(); project.setId(1L); project.setOwner("alice");
        when(projectRepo.findById(1L)).thenReturn(Optional.of(project));
        when(userRepo.findById("bob")).thenReturn(Optional.of(new com.example.collab.auth.UserEntity("bob", "x", java.util.Set.of("VIEWER"))));
        when(permRepo.findByProjectIdAndUsername(1L, "bob")).thenReturn(Optional.empty());
        when(permRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        ProjectPermission saved = svc.shareProject(1L, "bob", "EDITOR", "alice");

        assertEquals("bob", saved.getUsername());
        assertEquals("EDITOR", saved.getRole());

        ArgumentCaptor<com.example.collab.dto.PermissionNotification> captor = ArgumentCaptor.forClass(com.example.collab.dto.PermissionNotification.class);
        verify(redisPublisher, times(1)).publishPermission(captor.capture());
        var sent = captor.getValue();
        assertEquals(1L, sent.getProjectId());
        assertEquals("bob", sent.getUsername());
        assertEquals("SHARED", sent.getAction());
        assertEquals("alice", sent.getPerformedBy());
    }
}

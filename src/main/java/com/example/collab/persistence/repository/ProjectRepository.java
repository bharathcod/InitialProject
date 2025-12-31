package com.example.collab.persistence.repository;

import com.example.collab.persistence.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}

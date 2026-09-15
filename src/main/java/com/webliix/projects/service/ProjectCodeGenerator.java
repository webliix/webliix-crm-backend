package com.webliix.projects.service;

import com.webliix.projects.entity.Project;
import com.webliix.projects.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProjectCodeGenerator {

    private final ProjectRepository projectRepository;

    public String generateNextProjectCode() {
        Optional<Project> latest = projectRepository.findTopByOrderByIdDesc();
        long nextNumber = latest.map(Project::getId).orElse(0L) + 1;
        return String.format("PRJ-%06d", nextNumber);
    }
}


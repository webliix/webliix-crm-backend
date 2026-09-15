package com.webliix.projects.repository;

import com.webliix.projects.entity.Project;
import com.webliix.projects.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findTopByOrderByIdDesc();

    Page<Project> findByProjectNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String projectName, String description, Pageable pageable);

    List<Project> findByCustomerId(Long customerId);

    long countByStatus(ProjectStatus status);

    long countByExpectedEndDateBeforeAndStatusNot(LocalDate date, ProjectStatus status);
}

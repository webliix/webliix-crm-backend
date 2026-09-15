package com.webliix.projects.repository;

import com.webliix.projects.entity.ProjectTask;
import com.webliix.projects.enums.ProjectTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {

    List<ProjectTask> findByProjectId(Long projectId);

    long countByProjectId(Long projectId);

    long countByProjectIdAndStatus(Long projectId, ProjectTaskStatus status);

    long countByProjectIdAndDueDateBeforeAndStatusNot(Long projectId, LocalDate date, ProjectTaskStatus status);

    long countByStatus(ProjectTaskStatus status);

    long countByDueDateBeforeAndStatusNot(LocalDate date, ProjectTaskStatus status);
}


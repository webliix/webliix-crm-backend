package com.webliix.projects.repository;

import com.webliix.projects.entity.ProjectMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProjectMilestoneRepository extends JpaRepository<ProjectMilestone, Long> {

    List<ProjectMilestone> findByProjectId(Long projectId);

    long countByProjectIdAndDueDateBeforeAndCompletedFalse(Long projectId, LocalDate date);

    long countByDueDateBeforeAndCompletedFalse(LocalDate date);
}


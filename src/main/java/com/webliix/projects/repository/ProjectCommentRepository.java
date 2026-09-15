package com.webliix.projects.repository;

import com.webliix.projects.entity.ProjectComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectCommentRepository extends JpaRepository<ProjectComment, Long> {

    List<ProjectComment> findByProjectId(Long projectId);

    List<ProjectComment> findByProjectIdAndTaskId(Long projectId, Long taskId);
}


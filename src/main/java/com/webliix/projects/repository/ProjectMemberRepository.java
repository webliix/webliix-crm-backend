package com.webliix.projects.repository;

import com.webliix.projects.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProjectId(Long projectId);

    List<ProjectMember> findByUserId(Long userId);

    List<ProjectMember> findByUserIdIn(java.util.Collection<Long> userIds);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    boolean existsByProjectIdAndUserIdIn(Long projectId, java.util.Collection<Long> userIds);
}

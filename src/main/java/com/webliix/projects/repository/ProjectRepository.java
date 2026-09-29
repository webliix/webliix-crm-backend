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

    Page<Project> findByCustomerEmail(String email, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Project p WHERE LOWER(p.customer.email) = LOWER(:email) AND (:search IS NULL OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Project> findByCustomerEmailWithSearch(@org.springframework.data.repository.query.Param("email") String email, @org.springframework.data.repository.query.Param("search") String search, Pageable pageable);

    long countByStatus(ProjectStatus status);

    long countByExpectedEndDateBeforeAndStatusNot(LocalDate date, ProjectStatus status);
}

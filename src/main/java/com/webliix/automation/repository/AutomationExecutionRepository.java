package com.webliix.automation.repository;

import com.webliix.automation.entity.AutomationExecution;
import com.webliix.automation.enums.AutomationExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationExecutionRepository extends JpaRepository<AutomationExecution, Long> {
    long countByStatus(AutomationExecutionStatus status);
    List<AutomationExecution> findTop50ByOrderByCreatedAtDesc();
}

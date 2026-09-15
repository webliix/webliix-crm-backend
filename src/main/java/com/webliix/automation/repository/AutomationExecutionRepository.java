package com.webliix.automation.repository;

import com.webliix.automation.entity.AutomationExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutomationExecutionRepository extends JpaRepository<AutomationExecution, Long> {
}

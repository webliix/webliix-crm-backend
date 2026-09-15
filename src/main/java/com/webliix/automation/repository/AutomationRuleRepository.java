package com.webliix.automation.repository;

import com.webliix.automation.entity.AutomationRule;
import com.webliix.automation.enums.TriggerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationRuleRepository extends JpaRepository<AutomationRule, Long> {
    List<AutomationRule> findByTriggerTypeAndEnabledTrue(TriggerType triggerType);
}

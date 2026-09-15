package com.webliix.automation.entity;

import com.webliix.automation.enums.AutomationExecutionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "automation_executions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutomationExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id")
    private Long ruleId;

    @Enumerated(EnumType.STRING)
    private AutomationExecutionStatus status;

    @Column(name = "execution_time")
    private LocalDateTime executionTime;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

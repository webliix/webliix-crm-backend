package com.webliix.automation.entity;

import com.webliix.automation.enums.TriggerType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "automation_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutomationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private TriggerType triggerType;

    @Column(name = "condition_json", columnDefinition = "TEXT")
    private String conditionJson;

    @Column(name = "action_json", columnDefinition = "TEXT")
    private String actionJson;

    private Boolean enabled;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

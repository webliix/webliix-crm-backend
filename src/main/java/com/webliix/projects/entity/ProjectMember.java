package com.webliix.projects.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "project_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "role_in_project")
    private String roleInProject;

    @Column(name = "assigned_date")
    private LocalDate assignedDate;
}


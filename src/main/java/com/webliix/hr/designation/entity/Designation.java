package com.webliix.hr.designation.entity;

import com.webliix.hr.department.entity.Department;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "designations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Designation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "designation_code", unique = true, nullable = false)
    private String designationCode;

    @Column(name = "designation_name", nullable = false)
    private String designationName;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(columnDefinition = "TEXT")
    private String description;
}

package com.webliix.careers.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_opening_id")
    private Long jobOpeningId;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(name = "application_type")
    @Builder.Default
    private String applicationType = "job";

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String phone;

    private String location;

    private String experience;

    @Column(name = "portfolio_url", columnDefinition = "TEXT")
    private String portfolioUrl;

    @Column(name = "resume_link", columnDefinition = "TEXT")
    private String resumeLink;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "payout_method")
    private String payoutMethod;

    @Column(name = "payout_details", columnDefinition = "TEXT")
    private String payoutDetails;

    @Column(nullable = false)
    @Builder.Default
    private String status = "NEW"; // NEW, REVIEWING, SHORTLISTED, REJECTED, HIRED

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "NEW";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

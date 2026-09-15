package com.webliix.notifications.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email_enabled", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean emailEnabled;

    @Column(name = "sms_enabled", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean smsEnabled;

    @Column(name = "whatsapp_enabled", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean whatsappEnabled;

    @Column(name = "push_enabled", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean pushEnabled;

    @Column(name = "ticket_notifications", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean ticketNotifications;

    @Column(name = "invoice_notifications", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean invoiceNotifications;

    @Column(name = "project_notifications", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean projectNotifications;

    @Column(name = "lead_notifications", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean leadNotifications;

    @Column(name = "payroll_notifications", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean payrollNotifications;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

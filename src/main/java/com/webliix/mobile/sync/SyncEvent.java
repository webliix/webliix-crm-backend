package com.webliix.mobile.sync;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sync_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column
    private String operation;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "device_id")
    private String deviceId;
}

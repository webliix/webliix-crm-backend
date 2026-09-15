package com.webliix.mobile.notification;

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
@Table(name = "mobile_notification_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MobileNotificationPreference {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tenant_id")
	private Long tenantId;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "email_enabled")
	private Boolean emailEnabled = true;

	@Column(name = "push_enabled")
	private Boolean pushEnabled = true;

	@Column(name = "sms_enabled")
	private Boolean smsEnabled = false;

	@Column(name = "in_app_enabled")
	private Boolean inAppEnabled = true;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}

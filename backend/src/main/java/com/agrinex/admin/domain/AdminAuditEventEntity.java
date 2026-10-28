package com.agrinex.admin.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One audited admin action (table {@code admin_audit_events}; ADR-0020 item 6). Append-only and kept
 * indefinitely (ADR-0029). {@code details} holds masked before and after values as JSON text and never a credential
 * ({@code ck_admin_audit_events_no_credentials}).
 */
@Entity
@Immutable
@Table(name = "admin_audit_events")
public class AdminAuditEventEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "actor_user_id")
	private UUID actorUserId;

	@Enumerated(EnumType.STRING)
	private AuditAction action;

	@Column(name = "target_type")
	private String targetType;

	@Column(name = "target_id")
	private String targetId;

	@JdbcTypeCode(SqlTypes.JSON)
	private String details;

	@Column(name = "correlation_id")
	private String correlationId;

	@Column(name = "created_at")
	private Instant createdAt;

	protected AdminAuditEventEntity() {
	}

	public AdminAuditEventEntity(UUID actorUserId, AuditAction action, AuditTarget target, String targetId,
			String details, String correlationId, Instant createdAt) {
		this.actorUserId = actorUserId;
		this.action = action;
		this.targetType = target.type();
		this.targetId = targetId;
		this.details = details;
		this.correlationId = correlationId;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return this.id;
	}

	public UUID getActorUserId() {
		return this.actorUserId;
	}

	public AuditAction getAction() {
		return this.action;
	}

	public String getTargetType() {
		return this.targetType;
	}

	public String getTargetId() {
		return this.targetId;
	}

	public String getDetails() {
		return this.details;
	}

	public String getCorrelationId() {
		return this.correlationId;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

}

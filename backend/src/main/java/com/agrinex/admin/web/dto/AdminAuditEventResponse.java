package com.agrinex.admin.web.dto;

import java.time.Instant;
import java.util.UUID;

import tools.jackson.databind.JsonNode;

import com.agrinex.admin.domain.AuditAction;
import com.agrinex.admin.service.AuditQueryService.AuditEvent;

/** Contract AdminAuditEventResponse (ADR-0020 item 6): only the actor's id, never personal data or credentials. */
public record AdminAuditEventResponse(UUID id, AuditAction action, Actor actor, String targetType, String targetId,
		JsonNode details, String correlationId, Instant createdAt) {

	public static AdminAuditEventResponse from(AuditEvent event) {
		return new AdminAuditEventResponse(event.id(), event.action(), new Actor(event.actorUserId()),
				event.targetType(), event.targetId(), event.details(), event.correlationId(), event.createdAt());
	}

	/** The ADMIN who acted. */
	public record Actor(UUID userId) {
	}

}

package com.agrinex.admin.service;

import java.time.Instant;
import java.util.UUID;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import com.agrinex.admin.domain.AdminAuditEventEntity;
import com.agrinex.admin.domain.AuditAction;
import com.agrinex.admin.repository.AdminAuditEventRepository;
import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The admin audit log, newest first (ADR-0020 item 6; contract listAdminAuditEvents). */
@Service
@Transactional(readOnly = true)
public class AuditQueryService {

	/** createdAt descending, then id (contract listAdminAuditEvents). */
	static final Sort AUDIT_ORDER = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id"));

	private final AdminAuditEventRepository events;

	private final JsonMapper jsonMapper;

	AuditQueryService(AdminAuditEventRepository events, JsonMapper jsonMapper) {
		this.events = events;
		this.jsonMapper = jsonMapper;
	}

	public PageResponse<AuditEvent> list(AuditAction action, PageParams page) {
		PageRequest request = PageRequest.of(page.page(), page.size(), AUDIT_ORDER);
		Page<AdminAuditEventEntity> result = (action != null) ? this.events.findByAction(action, request)
				: this.events.findAll(request);
		return page.toResponse(result.getContent().stream().map(this::toEvent).toList(), result.getTotalElements());
	}

	private AuditEvent toEvent(AdminAuditEventEntity entity) {
		return new AuditEvent(entity.getId(), entity.getAction(), entity.getActorUserId(), entity.getTargetType(),
				entity.getTargetId(), this.jsonMapper.readTree(entity.getDetails()), entity.getCorrelationId(),
				entity.getCreatedAt());
	}

	/** One audit event; {@code details} is the stored JSON object. */
	public record AuditEvent(UUID id, AuditAction action, UUID actorUserId, String targetType, String targetId,
			JsonNode details, String correlationId, Instant createdAt) {
	}

}

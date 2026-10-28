package com.agrinex.admin.service;

import java.util.Map;
import java.util.UUID;

import tools.jackson.databind.json.JsonMapper;

import com.agrinex.admin.domain.AdminAuditEventEntity;
import com.agrinex.admin.domain.AuditAction;
import com.agrinex.admin.domain.AuditTarget;
import com.agrinex.admin.repository.AdminAuditEventRepository;
import com.agrinex.common.logging.CorrelationId;
import com.agrinex.common.time.AgrinexCalendar;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes the audit row of an admin action in the same transaction as the change it records (ADR-0020 item 6), so
 * a change is never committed without its audit row. Details hold masked before and after values and never a
 * credential or personal data beyond what the action's decision allows (ADR-0037 item 8, ADR-0041 item 7).
 */
@Component
public class AdminAuditLog {

	private final AdminAuditEventRepository events;

	private final JsonMapper jsonMapper;

	private final AgrinexCalendar calendar;

	AdminAuditLog(AdminAuditEventRepository events, JsonMapper jsonMapper, AgrinexCalendar calendar) {
		this.events = events;
		this.jsonMapper = jsonMapper;
		this.calendar = calendar;
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void record(UUID actorUserId, AuditAction action, AuditTarget target, Object targetId,
			Map<String, ?> details) {
		this.events.save(new AdminAuditEventEntity(actorUserId, action, target,
				(targetId != null) ? targetId.toString() : null, this.jsonMapper.writeValueAsString(details),
				CorrelationId.current(), this.calendar.now()));
	}

}

package com.agrinex.admin.web;

import com.agrinex.admin.domain.AuditAction;
import com.agrinex.admin.service.AuditQueryService;
import com.agrinex.admin.web.dto.AdminAuditEventResponse;
import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The admin audit log (contract tag Admin - Curation; ADR-0020 item 6). ADMIN only, read-only. */
@RestController
@RequestMapping("/api/v1/admin/audit-events")
@PreAuthorize("hasRole('ADMIN')")
public class AuditEventController {

	private final AuditQueryService audit;

	AuditEventController(AuditQueryService audit) {
		this.audit = audit;
	}

	@GetMapping
	public PageResponse<AdminAuditEventResponse> listAdminAuditEvents(
			@RequestParam(required = false) AuditAction action, @RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		return this.audit.list(action, PageParams.of(page, size)).map(AdminAuditEventResponse::from);
	}

}

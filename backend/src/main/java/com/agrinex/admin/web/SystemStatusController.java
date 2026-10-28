package com.agrinex.admin.web;

import com.agrinex.admin.service.SystemStatusService;
import com.agrinex.admin.web.dto.SystemStatusResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only system status (contract tag Admin - System; ADR-0020 items 2 and 3). ADMIN only. */
@RestController
@RequestMapping("/api/v1/admin/system")
@PreAuthorize("hasRole('ADMIN')")
public class SystemStatusController {

	private final SystemStatusService status;

	SystemStatusController(SystemStatusService status) {
		this.status = status;
	}

	@GetMapping("/status")
	public SystemStatusResponse getSystemStatus() {
		return SystemStatusResponse.from(this.status.status());
	}

}

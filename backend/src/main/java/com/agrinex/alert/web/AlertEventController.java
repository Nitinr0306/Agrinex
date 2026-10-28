package com.agrinex.alert.web;

import java.util.UUID;

import com.agrinex.alert.service.AlertEventService;
import com.agrinex.alert.web.dto.AlertEventPageResponse;
import com.agrinex.auth.CurrentAccount;
import com.agrinex.common.web.PageParams;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** In-app alert events, delivered by polling (contract tag Alerts; ADR-0019 item 5). FARMER and ADMIN only. */
@RestController
@RequestMapping("/api/v1/alerts/events")
@PreAuthorize("hasAnyRole('FARMER', 'ADMIN')")
public class AlertEventController {

	private final AlertEventService events;

	AlertEventController(AlertEventService events) {
		this.events = events;
	}

	@GetMapping
	public AlertEventPageResponse listAlertEvents(@AuthenticationPrincipal CurrentAccount caller,
			@RequestParam(defaultValue = "false") boolean unreadOnly, @RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		return AlertEventPageResponse
			.from(this.events.list(caller.userId(), unreadOnly, PageParams.of(page, size)));
	}

	@PostMapping("/{eventId}/read")
	public ResponseEntity<Void> markAlertEventRead(@AuthenticationPrincipal CurrentAccount caller,
			@PathVariable UUID eventId) {
		this.events.markRead(caller.userId(), eventId);
		return ResponseEntity.noContent().build();
	}

}

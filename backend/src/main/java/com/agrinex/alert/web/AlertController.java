package com.agrinex.alert.web;

import java.util.UUID;

import jakarta.validation.Valid;

import com.agrinex.alert.domain.AlertStatus;
import com.agrinex.alert.service.AlertService;
import com.agrinex.alert.web.dto.AlertResponse;
import com.agrinex.alert.web.dto.CreateAlertRequest;
import com.agrinex.alert.web.dto.UpdateAlertStatusRequest;
import com.agrinex.auth.CurrentAccount;
import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The caller's price alerts (contract tag Alerts; spec section 34). FARMER and ADMIN only. */
@RestController
@RequestMapping("/api/v1/alerts")
@PreAuthorize("hasAnyRole('FARMER', 'ADMIN')")
public class AlertController {

	private final AlertService alerts;

	AlertController(AlertService alerts) {
		this.alerts = alerts;
	}

	@GetMapping
	public PageResponse<AlertResponse> listAlerts(@AuthenticationPrincipal CurrentAccount caller,
			@RequestParam(required = false) AlertStatus status, @RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		return this.alerts.list(caller.userId(), status, PageParams.of(page, size)).map(AlertResponse::from);
	}

	@PostMapping
	public ResponseEntity<AlertResponse> createAlert(@AuthenticationPrincipal CurrentAccount caller,
			@Valid @RequestBody CreateAlertRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(AlertResponse.from(this.alerts.create(caller.userId(), request.toNewAlert())));
	}

	@PatchMapping("/{alertId}")
	public AlertResponse updateAlertStatus(@AuthenticationPrincipal CurrentAccount caller, @PathVariable UUID alertId,
			@Valid @RequestBody UpdateAlertStatusRequest request) {
		return AlertResponse.from(this.alerts.updateStatus(caller.userId(), alertId, request.status()));
	}

	@DeleteMapping("/{alertId}")
	public ResponseEntity<Void> deleteAlert(@AuthenticationPrincipal CurrentAccount caller,
			@PathVariable UUID alertId) {
		this.alerts.delete(caller.userId(), alertId);
		return ResponseEntity.noContent().build();
	}

}

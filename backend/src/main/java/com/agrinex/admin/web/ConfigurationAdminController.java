package com.agrinex.admin.web;

import jakarta.validation.Valid;

import com.agrinex.admin.service.ConfigurationAdminService;
import com.agrinex.admin.web.dto.TransportAssumptionResponse;
import com.agrinex.admin.web.dto.TransportAssumptionsUpdateRequest;
import com.agrinex.admin.web.dto.WeightsResponse;
import com.agrinex.admin.web.dto.WeightsUpdateRequest;
import com.agrinex.auth.CurrentAccount;
import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Versioned transport assumptions and recommendation weights (contract tag Admin - Configuration; spec section 30,
 * 32; ADR-0020 item 1). ADMIN only; every update inserts a new version and is audited.
 */
@RestController
@RequestMapping("/api/v1/admin/config")
@PreAuthorize("hasRole('ADMIN')")
public class ConfigurationAdminController {

	private final ConfigurationAdminService configuration;

	ConfigurationAdminController(ConfigurationAdminService configuration) {
		this.configuration = configuration;
	}

	@GetMapping("/transport-assumptions")
	public TransportAssumptionResponse getTransportAssumptions() {
		return TransportAssumptionResponse.from(this.configuration.transportAssumptions());
	}

	@PutMapping("/transport-assumptions")
	public TransportAssumptionResponse updateTransportAssumptions(@AuthenticationPrincipal CurrentAccount caller,
			@Valid @RequestBody TransportAssumptionsUpdateRequest request) {
		return TransportAssumptionResponse.from(this.configuration.updateTransportAssumptions(caller.userId(),
				request.ratePerKm(), request.minimumTripCharge(), request.vehicleCapacityKg()));
	}

	@GetMapping("/transport-assumptions/versions")
	public PageResponse<TransportAssumptionResponse> listTransportAssumptionVersions(
			@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
		return this.configuration.transportVersions(PageParams.of(page, size)).map(TransportAssumptionResponse::from);
	}

	@GetMapping("/recommendation-weights")
	public WeightsResponse getRecommendationWeights() {
		return WeightsResponse.from(this.configuration.weights());
	}

	@PutMapping("/recommendation-weights")
	public WeightsResponse updateRecommendationWeights(@AuthenticationPrincipal CurrentAccount caller,
			@Valid @RequestBody WeightsUpdateRequest request) {
		return WeightsResponse.from(this.configuration.updateWeights(caller.userId(), request.toUpdate()));
	}

	@GetMapping("/recommendation-weights/versions")
	public PageResponse<WeightsResponse> listRecommendationWeightVersions(
			@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
		return this.configuration.weightVersions(PageParams.of(page, size)).map(WeightsResponse::from);
	}

}

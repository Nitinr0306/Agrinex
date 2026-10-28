package com.agrinex.geography.web;

import java.util.UUID;

import com.agrinex.common.error.ApiException;
import com.agrinex.geography.GeographyApi;
import com.agrinex.geography.web.dto.DistrictListResponse;
import com.agrinex.geography.web.dto.DistrictResponse;
import com.agrinex.geography.web.dto.LocalityListResponse;
import com.agrinex.geography.web.dto.StateListResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Geography reference lists for the location wizard (contract tag Geography; F1.3). Anonymous. */
@RestController
@RequestMapping("/api/v1/geography")
class GeographyController {

	private final GeographyApi geography;

	GeographyController(GeographyApi geography) {
		this.geography = geography;
	}

	@GetMapping("/states")
	StateListResponse listStates() {
		return new StateListResponse(this.geography.listStates());
	}

	@GetMapping("/states/{stateId}/districts")
	DistrictListResponse listDistricts(@PathVariable UUID stateId) {
		return new DistrictListResponse(this.geography.listDistricts(stateId)
			.orElseThrow(() -> ApiException.notFound("The state does not exist."))
			.stream()
			.map(DistrictResponse::from)
			.toList());
	}

	@GetMapping("/districts/{districtId}/localities")
	LocalityListResponse listLocalities(@PathVariable UUID districtId) {
		return new LocalityListResponse(this.geography.listLocalities(districtId)
			.orElseThrow(() -> ApiException.notFound("The district does not exist.")));
	}

}

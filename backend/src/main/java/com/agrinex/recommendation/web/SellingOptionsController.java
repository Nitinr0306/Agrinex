package com.agrinex.recommendation.web;

import jakarta.validation.Valid;

import com.agrinex.common.ratelimit.RateLimited;
import com.agrinex.common.ratelimit.RateLimited.Bucket;
import com.agrinex.recommendation.service.ComparisonService;
import com.agrinex.recommendation.service.SimulationService;
import com.agrinex.recommendation.web.dto.CompareRequest;
import com.agrinex.recommendation.web.dto.MarketComparisonResponse;
import com.agrinex.recommendation.web.dto.SimulateRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Market comparison and server-side What-If (contract tag Selling options; spec section 16, 17). Anonymous
 * callers are allowed; a sent access token is resolved to its account (ADR-0042). Compare and simulate share one
 * rate-limit bucket of 60 requests per minute per client IP (spec section 28).
 */
@RestController
@RequestMapping("/api/v1/selling-options")
class SellingOptionsController {

	private final ComparisonService comparisons;

	private final SimulationService simulations;

	SellingOptionsController(ComparisonService comparisons, SimulationService simulations) {
		this.comparisons = comparisons;
		this.simulations = simulations;
	}

	@PostMapping("/compare")
	@RateLimited(Bucket.SELLING_OPTIONS)
	MarketComparisonResponse compareSellingOptions(@Valid @RequestBody CompareRequest request) {
		return MarketComparisonResponse.from(this.comparisons.compare(request.toCommand()));
	}

	@PostMapping("/simulate")
	@RateLimited(Bucket.SELLING_OPTIONS)
	MarketComparisonResponse simulateSellingOptions(@Valid @RequestBody SimulateRequest request) {
		return MarketComparisonResponse
			.from(this.simulations.simulate(request.recommendationId(), request.toOverrides()));
	}

}

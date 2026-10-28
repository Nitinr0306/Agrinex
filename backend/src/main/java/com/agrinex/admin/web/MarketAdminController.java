package com.agrinex.admin.web;

import java.util.UUID;

import jakarta.validation.Valid;

import com.agrinex.admin.service.MarketAdminService;
import com.agrinex.admin.web.dto.AdminMarketDetailResponse;
import com.agrinex.admin.web.dto.AdminMarketResponse;
import com.agrinex.admin.web.dto.MergeMarketRequest;
import com.agrinex.admin.web.dto.SetMarketCoordinatesRequest;
import com.agrinex.auth.CurrentAccount;
import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;
import com.agrinex.price.MarketReviewStatus;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Market curation with evidence (contract tag Admin - Curation; ADR-0014 item 4, ADR-0020). ADMIN only; every
 * change is audited. Never automatic: no fuzzy matching and no geocoding.
 */
@RestController
@RequestMapping("/api/v1/admin/markets")
@PreAuthorize("hasRole('ADMIN')")
public class MarketAdminController {

	private final MarketAdminService markets;

	private final SearchParameter searchParameter;

	MarketAdminController(MarketAdminService markets, SearchParameter searchParameter) {
		this.markets = markets;
		this.searchParameter = searchParameter;
	}

	@GetMapping
	public PageResponse<AdminMarketResponse> listAdminMarkets(
			@RequestParam(required = false) MarketReviewStatus reviewStatus,
			@RequestParam(required = false) Boolean active, @RequestParam(required = false) UUID districtId,
			@RequestParam(required = false) String search, @RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {
		return this.markets
			.list(reviewStatus, active, districtId, this.searchParameter.validated(search), PageParams.of(page, size))
			.map(AdminMarketResponse::from);
	}

	@GetMapping("/{marketId}")
	public AdminMarketDetailResponse getAdminMarket(@PathVariable UUID marketId) {
		return AdminMarketDetailResponse.from(this.markets.detail(marketId));
	}

	@PostMapping("/{marketId}/verify")
	public AdminMarketResponse verifyMarket(@AuthenticationPrincipal CurrentAccount caller,
			@PathVariable UUID marketId) {
		return AdminMarketResponse.from(this.markets.verify(caller.userId(), marketId));
	}

	@PostMapping("/{marketId}/merge")
	public AdminMarketResponse mergeMarket(@AuthenticationPrincipal CurrentAccount caller, @PathVariable UUID marketId,
			@Valid @RequestBody MergeMarketRequest request) {
		return AdminMarketResponse.from(
				this.markets.merge(caller.userId(), marketId, request.targetMarketId(), request.evidenceNote()));
	}

	@PostMapping("/{marketId}/deactivate")
	public AdminMarketResponse deactivateMarket(@AuthenticationPrincipal CurrentAccount caller,
			@PathVariable UUID marketId) {
		return AdminMarketResponse.from(this.markets.deactivate(caller.userId(), marketId));
	}

	@PostMapping("/{marketId}/reactivate")
	public AdminMarketResponse reactivateMarket(@AuthenticationPrincipal CurrentAccount caller,
			@PathVariable UUID marketId) {
		return AdminMarketResponse.from(this.markets.reactivate(caller.userId(), marketId));
	}

	@PutMapping("/{marketId}/coordinates")
	public AdminMarketResponse setMarketCoordinates(@AuthenticationPrincipal CurrentAccount caller,
			@PathVariable UUID marketId, @Valid @RequestBody SetMarketCoordinatesRequest request) {
		return AdminMarketResponse.from(this.markets.setCoordinates(caller.userId(), marketId, request.lat(),
				request.lon(), request.coordinateSource()));
	}

}

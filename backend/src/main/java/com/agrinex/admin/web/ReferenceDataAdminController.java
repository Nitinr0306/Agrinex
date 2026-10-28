package com.agrinex.admin.web;

import java.util.UUID;

import jakarta.validation.Valid;

import com.agrinex.admin.service.ReferenceDataAdminService;
import com.agrinex.admin.web.dto.AddCommodityRequest;
import com.agrinex.admin.web.dto.AddDistrictAliasRequest;
import com.agrinex.admin.web.dto.AdminCommodityResponse;
import com.agrinex.admin.web.dto.DistrictAliasResponse;
import com.agrinex.admin.web.dto.UpdateCommodityNamesRequest;
import com.agrinex.admin.web.dto.VerifyPriceUnitRequest;
import com.agrinex.auth.CurrentAccount;
import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * Commodity known list and district source aliases (contract tag Admin - Curation; ADR-0006, ADR-0008, ADR-0014
 * item 3, ADR-0015). ADMIN only; every change is audited with its evidence.
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class ReferenceDataAdminController {

	private final ReferenceDataAdminService referenceData;

	private final SearchParameter searchParameter;

	ReferenceDataAdminController(ReferenceDataAdminService referenceData, SearchParameter searchParameter) {
		this.referenceData = referenceData;
		this.searchParameter = searchParameter;
	}

	@GetMapping("/commodities")
	public PageResponse<AdminCommodityResponse> listAdminCommodities(
			@RequestParam(required = false) Boolean unitVerified, @RequestParam(required = false) String search,
			@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
		return this.referenceData
			.listCommodities(unitVerified, this.searchParameter.validated(search), PageParams.of(page, size))
			.map(AdminCommodityResponse::from);
	}

	@PostMapping("/commodities")
	public ResponseEntity<AdminCommodityResponse> addCommodity(@AuthenticationPrincipal CurrentAccount caller,
			@Valid @RequestBody AddCommodityRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(AdminCommodityResponse.from(this.referenceData.addCommodity(caller.userId(), request.name(),
					request.category(), request.priceUnit(), request.priceUnitSource(), request.sourceReference())));
	}

	@PutMapping("/commodities/{commodityId}/price-unit")
	public AdminCommodityResponse verifyCommodityPriceUnit(@AuthenticationPrincipal CurrentAccount caller,
			@PathVariable UUID commodityId, @Valid @RequestBody VerifyPriceUnitRequest request) {
		return AdminCommodityResponse.from(this.referenceData.verifyPriceUnit(caller.userId(), commodityId,
				request.priceUnit(), request.priceUnitSource()));
	}

	@PutMapping("/commodities/{commodityId}/names")
	public AdminCommodityResponse updateCommodityNames(@AuthenticationPrincipal CurrentAccount caller,
			@PathVariable UUID commodityId, @Valid @RequestBody UpdateCommodityNamesRequest request) {
		return AdminCommodityResponse.from(this.referenceData.updateNames(caller.userId(), commodityId,
				request.nameHi(), request.nameMr(), request.provenanceNote()));
	}

	@GetMapping("/district-aliases")
	public PageResponse<DistrictAliasResponse> listDistrictAliases(@RequestParam(required = false) UUID districtId,
			@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
		return this.referenceData.listDistrictAliases(districtId, PageParams.of(page, size))
			.map(DistrictAliasResponse::from);
	}

	@PostMapping("/district-aliases")
	public ResponseEntity<DistrictAliasResponse> addDistrictAlias(@AuthenticationPrincipal CurrentAccount caller,
			@Valid @RequestBody AddDistrictAliasRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(DistrictAliasResponse.from(this.referenceData.addDistrictAlias(caller.userId(),
					request.sourceStateName(), request.sourceDistrictName(), request.districtId(),
					request.evidenceNote())));
	}

}

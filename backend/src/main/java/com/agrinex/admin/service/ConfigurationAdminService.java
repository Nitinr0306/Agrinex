package com.agrinex.admin.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.agrinex.admin.domain.AuditAction;
import com.agrinex.admin.domain.AuditTarget;
import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;
import com.agrinex.recommendation.RecommendationApi;
import com.agrinex.recommendation.WeightsUpdate;
import com.agrinex.recommendation.WeightsVersion;
import com.agrinex.transport.TransportApi;
import com.agrinex.transport.TransportAssumptions;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Versioned configuration (spec section 30, 32; ADR-0020 item 1, ADR-0024): every update inserts a new active
 * version through the owning module and is audited with the previous and the new values, so each recommendation
 * run keeps the versions it used.
 */
@Service
public class ConfigurationAdminService {

	private final TransportApi transport;

	private final RecommendationApi recommendation;

	private final AdminAuditLog audit;

	ConfigurationAdminService(TransportApi transport, RecommendationApi recommendation, AdminAuditLog audit) {
		this.transport = transport;
		this.recommendation = recommendation;
		this.audit = audit;
	}

	public TransportAssumptions transportAssumptions() {
		return this.transport.activeAssumptions();
	}

	public PageResponse<TransportAssumptions> transportVersions(PageParams page) {
		return this.transport.listVersions(page);
	}

	@Transactional
	public TransportAssumptions updateTransportAssumptions(UUID actorUserId, BigDecimal ratePerKm,
			BigDecimal minimumTripCharge, BigDecimal vehicleCapacityKg) {
		TransportAssumptions before = this.transport.activeAssumptions();
		TransportAssumptions after = this.transport.updateAssumptions(ratePerKm, minimumTripCharge, vehicleCapacityKg);
		this.audit.record(actorUserId, AuditAction.TRANSPORT_ASSUMPTIONS_UPDATED, AuditTarget.TRANSPORT_ASSUMPTIONS,
				after.versionId(), Map.of("before", values(before), "after", values(after)));
		return after;
	}

	public WeightsVersion weights() {
		return this.recommendation.activeWeights();
	}

	public PageResponse<WeightsVersion> weightVersions(PageParams page) {
		return this.recommendation.listWeightVersions(page);
	}

	@Transactional
	public WeightsVersion updateWeights(UUID actorUserId, WeightsUpdate update) {
		WeightsVersion before = this.recommendation.activeWeights();
		WeightsVersion after = this.recommendation.updateWeights(update);
		this.audit.record(actorUserId, AuditAction.RECOMMENDATION_WEIGHTS_UPDATED, AuditTarget.RECOMMENDATION_WEIGHTS,
				after.versionId(), Map.of("before", values(before), "after", values(after)));
		return after;
	}

	private static Map<String, Object> values(TransportAssumptions version) {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("versionId", version.versionId().toString());
		values.put("ratePerKm", version.ratePerKm());
		values.put("minimumTripCharge", version.minimumTripCharge());
		values.put("vehicleCapacityKg", version.vehicleCapacityKg());
		return values;
	}

	private static Map<String, Object> values(WeightsVersion version) {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("versionId", version.versionId().toString());
		values.put("wNetRealization", version.wNetRealization());
		values.put("wPrice", version.wPrice());
		values.put("wTrend", version.wTrend());
		values.put("wFreshness", version.wFreshness());
		values.put("wTransportPenalty", version.wTransportPenalty());
		values.put("freshnessFreshDays", version.freshnessFreshDays());
		values.put("freshnessRecentDays", version.freshnessRecentDays());
		values.put("freshnessStaleDays", version.freshnessStaleDays());
		return values;
	}

}

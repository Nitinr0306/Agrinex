package com.agrinex.recommendation.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.FieldErrorCode;
import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;
import com.agrinex.price.FreshnessPolicyPort;
import com.agrinex.price.FreshnessThresholds;
import com.agrinex.recommendation.RecommendationApi;
import com.agrinex.recommendation.WeightsUpdate;
import com.agrinex.recommendation.WeightsVersion;
import com.agrinex.recommendation.domain.RecommendationWeightsEntity;
import com.agrinex.recommendation.mapper.RecommendationMapper;
import com.agrinex.recommendation.repository.RecommendationWeightsRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Versioned scoring weights and freshness thresholds (spec section 16; ADR-0020 item 1, ADR-0024). Also the
 * implementation of the price module's freshness port: market detail, trends, alerts and the commodity filter
 * classify freshness with the active version's thresholds (ADR-0035 item 1b).
 */
@Service
@Transactional(readOnly = true)
public class WeightsService implements RecommendationApi, FreshnessPolicyPort {

	private final RecommendationWeightsRepository versions;

	private final AgrinexCalendar calendar;

	WeightsService(RecommendationWeightsRepository versions, AgrinexCalendar calendar) {
		this.versions = versions;
		this.calendar = calendar;
	}

	@Override
	public WeightsVersion activeWeights() {
		return this.versions.findActive()
			.map(RecommendationMapper::toView)
			.orElseThrow(() -> new IllegalStateException("No active recommendation weights version exists"));
	}

	@Override
	public FreshnessThresholds activeThresholds() {
		return activeWeights().thresholds();
	}

	public Optional<WeightsVersion> findVersion(UUID versionId) {
		return this.versions.findById(versionId).map(RecommendationMapper::toView);
	}

	@Override
	@Transactional
	public WeightsVersion updateWeights(WeightsUpdate update) {
		if (update.freshnessRecentDays() < update.freshnessFreshDays()) {
			throw ApiException.validation("freshnessRecentDays", FieldErrorCode.INVALID_RANGE,
					"must not be less than freshnessFreshDays");
		}
		if (update.freshnessStaleDays() < update.freshnessRecentDays()) {
			throw ApiException.validation("freshnessStaleDays", FieldErrorCode.INVALID_RANGE,
					"must not be less than freshnessRecentDays");
		}
		this.versions.lockForVersionUpdate();
		this.versions.deactivateAll();
		RecommendationWeightsEntity created = new RecommendationWeightsEntity(update, this.calendar.now());
		return RecommendationMapper.toView(this.versions.saveAndFlush(created));
	}

	@Override
	public PageResponse<WeightsVersion> listWeightVersions(PageParams page) {
		var result = this.versions.findAllByOrderByCreatedAtDescIdAsc(PageRequest.of(page.page(), page.size()));
		List<WeightsVersion> items = result.getContent().stream().map(RecommendationMapper::toView).toList();
		return page.toResponse(items, result.getTotalElements());
	}

}

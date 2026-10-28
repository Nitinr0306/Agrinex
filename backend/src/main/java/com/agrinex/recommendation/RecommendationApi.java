package com.agrinex.recommendation;

import com.agrinex.common.web.PageParams;
import com.agrinex.common.web.PageResponse;

/**
 * Public interface of the recommendation module for the admin module (admin -> recommendation, ADR-0013):
 * the versioned weights and freshness thresholds (spec section 16, 30; ADR-0020 item 1, ADR-0024).
 */
public interface RecommendationApi {

	/** The version new comparisons use. */
	WeightsVersion activeWeights();

	/**
	 * Inserts a new active version; earlier versions stay unchanged.
	 * @throws com.agrinex.common.web.ApiException VALIDATION_FAILED (INVALID_RANGE, named on the later field) when
	 * the freshness days are not ordered fresh <= recent <= stale
	 */
	WeightsVersion updateWeights(WeightsUpdate update);

	/** Every version, newest first (createdAt descending, then id). */
	PageResponse<WeightsVersion> listWeightVersions(PageParams page);

}

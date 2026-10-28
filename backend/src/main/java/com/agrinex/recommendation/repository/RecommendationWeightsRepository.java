package com.agrinex.recommendation.repository;

import java.util.Optional;
import java.util.UUID;

import com.agrinex.recommendation.domain.RecommendationWeightsEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/** Repository of the recommendation module. Only this module's code uses it (spec section 27; ADR-0013). */
public interface RecommendationWeightsRepository extends JpaRepository<RecommendationWeightsEntity, UUID> {

	@Query("select w from RecommendationWeightsEntity w where w.active = true")
	Optional<RecommendationWeightsEntity> findActive();

	/** Newest first: createdAt descending, then id (API_CONVENTIONS.md section 5). */
	Page<RecommendationWeightsEntity> findAllByOrderByCreatedAtDescIdAsc(Pageable pageable);

	@Modifying(flushAutomatically = true)
	@Query("update RecommendationWeightsEntity w set w.active = false where w.active = true")
	int deactivateAll();

	/**
	 * Serializes concurrent version updates until the end of the transaction. Native SQL is required: an
	 * explicit table lock is the only way to make "deactivate the active version, insert the new one" safe
	 * against a concurrent update without violating the single-active-version index
	 * ({@code uq_recommendation_weights_single_active}). Reads are not blocked.
	 */
	@Modifying
	@Query(nativeQuery = true, value = "LOCK TABLE recommendation_weights IN SHARE ROW EXCLUSIVE MODE")
	void lockForVersionUpdate();

}

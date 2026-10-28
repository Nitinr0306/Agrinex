package com.agrinex.price.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import com.agrinex.price.MarketReviewStatus;
import com.agrinex.price.domain.MarketEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository of the price module. Only this module's code uses it (spec section 27; ADR-0013). */
public interface MarketRepository extends JpaRepository<MarketEntity, UUID> {

	boolean existsByCanonicalSlug(String canonicalSlug);

	List<MarketEntity> findByReviewStatusAndActiveTrue(MarketReviewStatus reviewStatus);

	/** Locks the market row for a curation change, so concurrent admin actions are serialized. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select m from MarketEntity m where m.id = :id")
	Optional<MarketEntity> findForUpdate(@Param("id") UUID id);

	/**
	 * Admin page ordered by name in code-point order, then id (API_CONVENTIONS.md section 5). Native SQL is
	 * required because code-point order needs {@code COLLATE "C"}, which JPQL cannot express, and the order must
	 * be applied in the database for correct paging. {@code search} is a case-insensitive substring of the name.
	 */
	@Query(nativeQuery = true, value = """
			SELECT * FROM markets
			WHERE (CAST(:reviewStatus AS text) IS NULL OR review_status = CAST(:reviewStatus AS text))
			  AND (CAST(:active AS boolean) IS NULL OR is_active = CAST(:active AS boolean))
			  AND (CAST(:districtId AS uuid) IS NULL OR district_id = CAST(:districtId AS uuid))
			  AND (CAST(:search AS text) IS NULL OR strpos(lower(canonical_name), lower(CAST(:search AS text))) > 0)
			ORDER BY canonical_name COLLATE "C", id
			LIMIT :limit OFFSET :offset
			""")
	List<MarketEntity> findAdminPage(@Param("reviewStatus") String reviewStatus, @Param("active") Boolean active,
			@Param("districtId") UUID districtId, @Param("search") String search, @Param("limit") int limit,
			@Param("offset") long offset);

	@Query(nativeQuery = true, value = """
			SELECT count(*) FROM markets
			WHERE (CAST(:reviewStatus AS text) IS NULL OR review_status = CAST(:reviewStatus AS text))
			  AND (CAST(:active AS boolean) IS NULL OR is_active = CAST(:active AS boolean))
			  AND (CAST(:districtId AS uuid) IS NULL OR district_id = CAST(:districtId AS uuid))
			  AND (CAST(:search AS text) IS NULL OR strpos(lower(canonical_name), lower(CAST(:search AS text))) > 0)
			""")
	long countAdminPage(@Param("reviewStatus") String reviewStatus, @Param("active") Boolean active,
			@Param("districtId") UUID districtId, @Param("search") String search);

}

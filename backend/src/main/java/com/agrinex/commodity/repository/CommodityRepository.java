package com.agrinex.commodity.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.commodity.domain.CommodityEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository of the commodity module. Only this module's code uses it (spec section 27; ADR-0013). */
public interface CommodityRepository extends JpaRepository<CommodityEntity, UUID> {

	Optional<CommodityEntity> findByCanonicalName(String canonicalName);

	/**
	 * Admin page ordered by name in code-point order, then id (API_CONVENTIONS.md section 5). Native SQL is
	 * required because code-point order needs {@code COLLATE "C"}, which JPQL cannot express, and the order must
	 * be applied in the database for correct paging.
	 */
	@Query(nativeQuery = true, value = """
			SELECT * FROM commodities
			WHERE (CAST(:unitVerified AS boolean) IS NULL OR (price_unit IS NOT NULL) = CAST(:unitVerified AS boolean))
			  AND (CAST(:search AS text) IS NULL OR strpos(lower(canonical_name), lower(CAST(:search AS text))) > 0)
			ORDER BY canonical_name COLLATE "C", id
			LIMIT :limit OFFSET :offset
			""")
	List<CommodityEntity> findPage(@Param("unitVerified") Boolean unitVerified, @Param("search") String search,
			@Param("limit") int limit, @Param("offset") long offset);

	@Query(nativeQuery = true, value = """
			SELECT count(*) FROM commodities
			WHERE (CAST(:unitVerified AS boolean) IS NULL OR (price_unit IS NOT NULL) = CAST(:unitVerified AS boolean))
			  AND (CAST(:search AS text) IS NULL OR strpos(lower(canonical_name), lower(CAST(:search AS text))) > 0)
			""")
	long countPage(@Param("unitVerified") Boolean unitVerified, @Param("search") String search);

}

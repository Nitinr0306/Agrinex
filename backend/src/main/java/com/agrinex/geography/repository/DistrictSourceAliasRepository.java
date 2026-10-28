package com.agrinex.geography.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.geography.domain.DistrictSourceAliasEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository of the geography module. Only this module's code uses it (spec section 27; ADR-0013). */
public interface DistrictSourceAliasRepository extends JpaRepository<DistrictSourceAliasEntity, UUID> {

	Optional<DistrictSourceAliasEntity> findBySourceStateNameAndSourceDistrictName(String sourceStateName,
			String sourceDistrictName);

	/**
	 * Admin page ordered by source state, then source district (code-point order), then id
	 * (API_CONVENTIONS.md section 5). Native SQL is required because code-point order needs {@code COLLATE "C"},
	 * which JPQL cannot express, and the order must be applied in the database for correct paging.
	 */
	@Query(nativeQuery = true, value = """
			SELECT * FROM district_source_aliases
			WHERE (CAST(:districtId AS uuid) IS NULL OR district_id = CAST(:districtId AS uuid))
			ORDER BY source_state_name COLLATE "C", source_district_name COLLATE "C", id
			LIMIT :limit OFFSET :offset
			""")
	List<DistrictSourceAliasEntity> findPage(@Param("districtId") UUID districtId, @Param("limit") int limit,
			@Param("offset") long offset);

	@Query(nativeQuery = true, value = """
			SELECT count(*) FROM district_source_aliases
			WHERE (CAST(:districtId AS uuid) IS NULL OR district_id = CAST(:districtId AS uuid))
			""")
	long countPage(@Param("districtId") UUID districtId);

}

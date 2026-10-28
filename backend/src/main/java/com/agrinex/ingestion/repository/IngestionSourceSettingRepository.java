package com.agrinex.ingestion.repository;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import com.agrinex.ingestion.domain.IngestionSourceSettingEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository of the ingestion module. Only this module's code uses it (spec section 27; ADR-0013). */
public interface IngestionSourceSettingRepository extends JpaRepository<IngestionSourceSettingEntity, UUID> {

	Optional<IngestionSourceSettingEntity> findBySourceDatasetId(UUID sourceDatasetId);

	/** Locks the dataset's setting, so concurrent pause and resume requests are serialized. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from IngestionSourceSettingEntity s where s.sourceDatasetId = :datasetId")
	Optional<IngestionSourceSettingEntity> findForUpdate(@Param("datasetId") UUID sourceDatasetId);

}

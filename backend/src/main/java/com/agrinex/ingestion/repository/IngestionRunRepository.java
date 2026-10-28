package com.agrinex.ingestion.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.ingestion.IngestionRunStatus;
import com.agrinex.ingestion.domain.IngestionRunEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository of the ingestion module. Only this module's code uses it (spec section 27; ADR-0013). Run lists are
 * ordered by the caller's {@link Pageable} sort (startedAt descending, then id).
 */
public interface IngestionRunRepository extends JpaRepository<IngestionRunEntity, UUID> {

	boolean existsBySourceDatasetIdAndStatus(UUID sourceDatasetId, IngestionRunStatus status);

	List<IngestionRunEntity> findByStatus(IngestionRunStatus status);

	Optional<IngestionRunEntity> findFirstBySourceDatasetIdAndStatus(UUID sourceDatasetId, IngestionRunStatus status);

	/** The most recent run that is no longer RUNNING. */
	Optional<IngestionRunEntity> findFirstBySourceDatasetIdAndStatusNotOrderByStartedAtDescIdAsc(UUID sourceDatasetId,
			IngestionRunStatus status);

	Page<IngestionRunEntity> findBySourceDatasetId(UUID sourceDatasetId, Pageable pageable);

	Page<IngestionRunEntity> findByStatus(IngestionRunStatus status, Pageable pageable);

	Page<IngestionRunEntity> findBySourceDatasetIdAndStatus(UUID sourceDatasetId, IngestionRunStatus status,
			Pageable pageable);

	boolean existsBySourceDatasetIdAndStatusInAndFinishedAtAfter(UUID sourceDatasetId,
			Collection<IngestionRunStatus> statuses, Instant after);

	/** End of the most recent run of the dataset with one of the statuses. */
	@Query("""
			select max(r.finishedAt) from IngestionRunEntity r
			where r.sourceDatasetId = :datasetId and r.status in :statuses
			""")
	Optional<Instant> findLatestFinishedAt(@Param("datasetId") UUID sourceDatasetId,
			@Param("statuses") Collection<IngestionRunStatus> statuses);

	/** Start of the most recent run of the dataset with one of the statuses. */
	@Query("""
			select max(r.startedAt) from IngestionRunEntity r
			where r.sourceDatasetId = :datasetId and r.status in :statuses
			""")
	Optional<Instant> findLatestStartedAt(@Param("datasetId") UUID sourceDatasetId,
			@Param("statuses") Collection<IngestionRunStatus> statuses);

	long countBySourceDatasetIdAndStatusAndStartedAtAfter(UUID sourceDatasetId, IngestionRunStatus status,
			Instant after);

	long countBySourceDatasetIdAndStatus(UUID sourceDatasetId, IngestionRunStatus status);

}

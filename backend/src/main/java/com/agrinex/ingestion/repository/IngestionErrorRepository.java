package com.agrinex.ingestion.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.agrinex.ingestion.IngestionErrorType;
import com.agrinex.ingestion.domain.IngestionErrorEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository of the ingestion module. Only this module's code uses it (spec section 27; ADR-0013). Error lists
 * are ordered by the caller's {@link Pageable} sort (createdAt ascending, then id).
 */
public interface IngestionErrorRepository extends JpaRepository<IngestionErrorEntity, UUID> {

	Page<IngestionErrorEntity> findByIngestionRunId(UUID runId, Pageable pageable);

	Page<IngestionErrorEntity> findByIngestionRunIdAndErrorType(UUID runId, IngestionErrorType errorType,
			Pageable pageable);

	Page<IngestionErrorEntity> findByIngestionRunIdAndErrorCode(UUID runId, String errorCode, Pageable pageable);

	Page<IngestionErrorEntity> findByIngestionRunIdAndErrorTypeAndErrorCode(UUID runId, IngestionErrorType errorType,
			String errorCode, Pageable pageable);

	@Query("""
			select new com.agrinex.ingestion.repository.RunErrorCount(e.ingestionRunId, count(e))
			from IngestionErrorEntity e where e.ingestionRunId in :runIds group by e.ingestionRunId
			""")
	List<RunErrorCount> countByRuns(@Param("runIds") Collection<UUID> runIds);

	/** Retention: error rows and their raw payloads are kept 180 days (ADR-0029 item 1). */
	@Modifying
	@Query("delete from IngestionErrorEntity e where e.createdAt < :cutoff")
	int deleteCreatedBefore(@Param("cutoff") Instant cutoff);

}

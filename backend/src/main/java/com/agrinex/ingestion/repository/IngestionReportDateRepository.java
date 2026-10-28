package com.agrinex.ingestion.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.ingestion.domain.IngestionReportDateEntity;

import org.springframework.data.jpa.repository.JpaRepository;

/** Report-date ledger of the ingestion module (ADR-0054 item 1). Only this module's code uses it (ADR-0013). */
public interface IngestionReportDateRepository extends JpaRepository<IngestionReportDateEntity, UUID> {

	Optional<IngestionReportDateEntity> findBySourceDatasetIdAndReportDate(UUID sourceDatasetId, LocalDate reportDate);

	List<IngestionReportDateEntity> findBySourceDatasetIdAndReportDateBetween(UUID sourceDatasetId, LocalDate from,
			LocalDate to);

	/** How many report dates a run has attempted so far: the progress of a RUNNING run. */
	long countByLastAttemptRunId(UUID runId);

}

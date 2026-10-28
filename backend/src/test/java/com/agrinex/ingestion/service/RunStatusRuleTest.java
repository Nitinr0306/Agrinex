package com.agrinex.ingestion.service;

import org.junit.jupiter.api.Test;

import com.agrinex.ingestion.IngestionRunStatus;
import com.agrinex.ingestion.domain.RunCounters;

import static org.assertj.core.api.Assertions.assertThat;

/** The run status rule of ADR-0011 item 8. */
class RunStatusRuleTest {

	@Test
	void everyPageFetchedIsSuccessEvenWithNoRecords() {
		RunCounters counters = new RunCounters();
		counters.pageFetched(0);
		assertThat(IngestionRunner.statusOf(counters, true, false)).isEqualTo(IngestionRunStatus.SUCCESS);
	}

	@Test
	void aLoadedUnitAndAFailedPageIsPartialFailure() {
		RunCounters counters = new RunCounters();
		counters.pageFetched(500);
		counters.pageFailed();
		assertThat(IngestionRunner.statusOf(counters, true, false)).isEqualTo(IngestionRunStatus.PARTIAL_FAILURE);
	}

	@Test
	void nothingFetchedIsFailed() {
		RunCounters counters = new RunCounters();
		counters.pageFailed();
		assertThat(IngestionRunner.statusOf(counters, false, false)).isEqualTo(IngestionRunStatus.FAILED);
	}

	@Test
	void aFailedLoadIsFailed() {
		RunCounters counters = new RunCounters();
		counters.pageFetched(10);
		assertThat(IngestionRunner.statusOf(counters, true, true)).isEqualTo(IngestionRunStatus.FAILED);
	}

}

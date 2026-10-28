package com.agrinex.admin.web.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

import com.agrinex.price.SourceDataset;

/**
 * Contract TriggerIngestionRequest (spec section 45; ADR-0011 item 3). The date-range rules are applied by the
 * ingestion module, which owns them.
 */
public record TriggerIngestionRequest(@NotNull SourceDataset datasetType, LocalDate fromDate, LocalDate toDate) {
}

package com.agrinex.price;

import java.util.UUID;

/** A registered source dataset (table source_datasets). */
public record SourceDatasetInfo(UUID id, SourceDataset type, String resourceId, String title) {
}

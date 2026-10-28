package com.agrinex.price.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agrinex.price.SourceDataset;
import com.agrinex.price.SourceDatasetInfo;
import com.agrinex.price.repository.SourceDatasetRepository;

import org.springframework.stereotype.Component;

/**
 * The two registered source datasets. They are reference data that change only through migrations, so they are
 * read once and kept in memory for the life of the application.
 */
@Component
class SourceDatasetCatalog {

	private final SourceDatasetRepository repository;

	private volatile List<SourceDatasetInfo> datasets;

	SourceDatasetCatalog(SourceDatasetRepository repository) {
		this.repository = repository;
	}

	List<SourceDatasetInfo> all() {
		List<SourceDatasetInfo> loaded = this.datasets;
		if (loaded == null) {
			loaded = this.repository.findAll()
				.stream()
				.map((entity) -> new SourceDatasetInfo(entity.getId(), entity.getDatasetType(), entity.getResourceId(),
						entity.getTitle()))
				.sorted(Comparator.comparing(SourceDatasetInfo::type))
				.toList();
			this.datasets = loaded;
		}
		return loaded;
	}

	SourceDatasetInfo byType(SourceDataset type) {
		return all().stream()
			.filter((dataset) -> dataset.type() == type)
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("Source dataset not registered: " + type));
	}

	SourceDatasetInfo byId(UUID id) {
		SourceDatasetInfo dataset = byIdMap().get(id);
		if (dataset == null) {
			throw new IllegalStateException("Unknown source dataset id");
		}
		return dataset;
	}

	private Map<UUID, SourceDatasetInfo> byIdMap() {
		return all().stream().collect(Collectors.toMap(SourceDatasetInfo::id, Function.identity()));
	}

}

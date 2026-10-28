package com.agrinex.price.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import com.agrinex.price.SourceDataset;

/** One of the two data.gov.in resources (table {@code source_datasets}; reference data, ADR-0006). */
@Entity
@Immutable
@Table(name = "source_datasets")
public class SourceDatasetEntity {

	@Id
	private UUID id;

	@Column(name = "resource_id")
	private String resourceId;

	private String title;

	@Enumerated(EnumType.STRING)
	@Column(name = "dataset_type")
	private SourceDataset datasetType;

	protected SourceDatasetEntity() {
	}

	public UUID getId() {
		return this.id;
	}

	public String getResourceId() {
		return this.resourceId;
	}

	public String getTitle() {
		return this.title;
	}

	public SourceDataset getDatasetType() {
		return this.datasetType;
	}

}

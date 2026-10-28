package com.agrinex.ingestion.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Pause state of a dataset's scheduled runs (table {@code ingestion_source_settings}; ADR-0020 item 1). Manual
 * triggers still work while paused. A reason is present exactly while paused.
 */
@Entity
@Table(name = "ingestion_source_settings")
public class IngestionSourceSettingEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "source_dataset_id")
	private UUID sourceDatasetId;

	@Column(name = "is_paused")
	private boolean paused;

	@Column(name = "paused_reason")
	private String pausedReason;

	@Column(name = "updated_at")
	private Instant updatedAt;

	protected IngestionSourceSettingEntity() {
	}

	public void pause(String reason, Instant now) {
		if (this.paused) {
			throw new IllegalStateException("The dataset is already paused");
		}
		this.paused = true;
		this.pausedReason = reason;
		this.updatedAt = now;
	}

	public void resume(Instant now) {
		if (!this.paused) {
			throw new IllegalStateException("The dataset is not paused");
		}
		this.paused = false;
		this.pausedReason = null;
		this.updatedAt = now;
	}

	public UUID getSourceDatasetId() {
		return this.sourceDatasetId;
	}

	public boolean isPaused() {
		return this.paused;
	}

	public String getPausedReason() {
		return this.pausedReason;
	}

	public Instant getUpdatedAt() {
		return this.updatedAt;
	}

}

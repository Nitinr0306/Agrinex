package com.agrinex.ingestion.pipeline;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.agrinex.commodity.CommodityApi;
import com.agrinex.geography.GeographyApi;
import com.agrinex.ingestion.domain.IngestionErrorCode;
import com.agrinex.ingestion.pipeline.RecordCheck.Accepted;
import com.agrinex.ingestion.pipeline.RecordCheck.Rejected;
import com.agrinex.ingestion.pipeline.UnitDeduplicator.ResolvedRecord;
import com.agrinex.price.PriceApi;

import org.springframework.stereotype.Component;

/**
 * Turns the fetched records of one delivery unit into load candidates (ADR-0011 item 7): coverage and rules V1
 * to V7 per record, V8 over the unit, then resolution of market, variety and grade for the records that passed,
 * then identity handling (ADR-0009 item 2). Markets, varieties and grades are registered only for records that
 * passed every rule.
 */
@Component
public class DeliveryUnitProcessor {

	private final RecordValidator validator;

	private final CommodityApi commodities;

	private final GeographyApi geography;

	private final PriceApi prices;

	DeliveryUnitProcessor(RecordValidator validator, CommodityApi commodities, GeographyApi geography,
			PriceApi prices) {
		this.validator = validator;
		this.commodities = commodities;
		this.geography = geography;
		this.prices = prices;
	}

	/** Fresh lookups for one run; registrations name the run in their evidence note (ADR-0014 item 4). */
	public ReferenceLookups lookupsFor(UUID ingestionRunId) {
		return new ReferenceLookups(this.commodities, this.geography, this.prices, ingestionRunId);
	}

	public ProcessedUnit process(List<SourceRecord> records, ReferenceLookups lookups, LocalDate today) {
		List<PendingError> errors = new ArrayList<>();
		List<ValidRecord> valid = new ArrayList<>();
		int outOfScope = 0;
		for (SourceRecord record : records) {
			RecordCheck check = this.validator.check(record, lookups, today);
			switch (check) {
				case Accepted accepted -> valid.add(accepted.record());
				case Rejected rejected -> errors.add(new PendingError(rejected.code(), rejected.message(),
						record.payloadJson()));
				case RecordCheck.OutOfScope ignored -> outOfScope++;
			}
		}
		OutlierRule.Split split = OutlierRule.apply(valid);
		split.outliers()
			.forEach((outlier) -> errors.add(new PendingError(IngestionErrorCode.PRICE_OUTLIER, outlier.message(),
					outlier.record().source().payloadJson())));
		int validationErrors = errors.size();
		List<ResolvedRecord> resolved = split.kept().stream().map((record) -> resolve(record, lookups)).toList();
		UnitDeduplicator.Result deduplicated = UnitDeduplicator.deduplicate(resolved);
		deduplicated.conflicts()
			.forEach((conflict) -> errors.add(new PendingError(IngestionErrorCode.SAME_KEY_DIFFERENT_PRICES,
					"Another record of the unit has the same identity with different prices",
					conflict.record().source().payloadJson())));
		return new ProcessedUnit(deduplicated.candidates(), errors, deduplicated.duplicates(), validationErrors,
				deduplicated.conflicts().size(), outOfScope);
	}

	private static ResolvedRecord resolve(ValidRecord record, ReferenceLookups lookups) {
		UUID commodityId = record.commodity().id();
		return new ResolvedRecord(record, lookups.market(record.source(), record.district()),
				lookups.variety(commodityId, record.source().variety()),
				lookups.grade(commodityId, record.source().grade()));
	}

}

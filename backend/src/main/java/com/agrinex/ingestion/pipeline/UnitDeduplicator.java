package com.agrinex.ingestion.pipeline;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.agrinex.price.ObservationCandidate;

/**
 * Identity handling inside one delivery unit (ADR-0009 item 2): records with the same identity and identical
 * prices give one candidate, and each extra copy, alias twins included, is a duplicate; records with the same
 * identity but different prices give no candidate and are all quarantined as conflicts. The candidates are
 * therefore conflict-free and unique, as the batch load requires (ADR-0009 item 5).
 */
final class UnitDeduplicator {

	private UnitDeduplicator() {
	}

	static Result deduplicate(List<ResolvedRecord> records) {
		Map<Identity, List<ResolvedRecord>> groups = new LinkedHashMap<>();
		records.forEach((record) -> groups.computeIfAbsent(record.identity(), (key) -> new ArrayList<>()).add(record));
		List<ObservationCandidate> candidates = new ArrayList<>();
		List<ResolvedRecord> conflicts = new ArrayList<>();
		int duplicates = 0;
		for (List<ResolvedRecord> group : groups.values()) {
			ResolvedRecord first = group.getFirst();
			if (group.stream().allMatch((record) -> samePrices(first.record(), record.record()))) {
				candidates.add(first.toCandidate());
				duplicates += group.size() - 1;
			}
			else {
				conflicts.addAll(group);
			}
		}
		return new Result(candidates, duplicates, conflicts);
	}

	private static boolean samePrices(ValidRecord a, ValidRecord b) {
		return a.modalPrice().compareTo(b.modalPrice()) == 0 && equal(a.minPrice(), b.minPrice())
				&& equal(a.maxPrice(), b.maxPrice());
	}

	private static boolean equal(BigDecimal a, BigDecimal b) {
		return (a == null) ? b == null : (b != null && a.compareTo(b) == 0);
	}

	/** A valid record with its resolved market, variety and grade (ADR-0009 item 1). */
	record ResolvedRecord(ValidRecord record, UUID marketId, UUID varietyId, UUID gradeId) {

		Identity identity() {
			return new Identity(this.marketId, this.record.commodity().id(), this.varietyId, this.gradeId,
					this.record.reportDate());
		}

		ObservationCandidate toCandidate() {
			return new ObservationCandidate(this.marketId, this.record.commodity().id(), this.varietyId, this.gradeId,
					this.record.reportDate(), this.record.modalPrice(), this.record.minPrice(), this.record.maxPrice(),
					this.record.commodity().priceUnit(), sourceCommodityName());
		}

		/** The source spelling when the record was resolved through a commodity alias, else null (ADR-0051). */
		private String sourceCommodityName() {
			String source = this.record.source().commodity();
			return (source == null || source.equals(this.record.commodity().name())) ? null : source;
		}

	}

	/** Market, commodity, variety, grade and report date; the dataset is the same for the whole unit. */
	record Identity(UUID marketId, UUID commodityId, UUID varietyId, UUID gradeId, LocalDate reportDate) {
	}

	/** The candidates to load, the duplicate copies counted, and the conflicting records. */
	record Result(List<ObservationCandidate> candidates, int duplicates, List<ResolvedRecord> conflicts) {
	}

}

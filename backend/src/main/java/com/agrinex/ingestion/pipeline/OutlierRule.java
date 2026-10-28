package com.agrinex.ingestion.pipeline;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Rule V8 of ADR-0008 item 5, over one complete delivery unit: for each commodity and report date with at least
 * five records that passed V1 to V7, a record whose modal is more than 10 times, or less than 0.1 times, the
 * group's median modal is rejected as PRICE_OUTLIER. The median is the middle value, or the mean of the two middle
 * values for an even count, in exact decimal arithmetic.
 */
final class OutlierRule {

	static final int MIN_GROUP_SIZE = 5;

	static final BigDecimal FACTOR = BigDecimal.TEN;

	private OutlierRule() {
	}

	static Split apply(List<ValidRecord> records) {
		Map<GroupKey, List<ValidRecord>> groups = new LinkedHashMap<>();
		records.forEach((record) -> groups
			.computeIfAbsent(new GroupKey(record.commodity().id(), record.reportDate()), (key) -> new ArrayList<>())
			.add(record));
		Set<ValidRecord> outliers = Collections.newSetFromMap(new IdentityHashMap<>());
		List<Outlier> rejected = new ArrayList<>();
		for (List<ValidRecord> group : groups.values()) {
			if (group.size() < MIN_GROUP_SIZE) {
				continue;
			}
			BigDecimal median = median(group.stream().map(ValidRecord::modalPrice).sorted().toList());
			for (ValidRecord record : group) {
				BigDecimal modal = record.modalPrice();
				if (modal.compareTo(median.multiply(FACTOR)) > 0 || modal.multiply(FACTOR).compareTo(median) < 0) {
					outliers.add(record);
					rejected.add(new Outlier(record, "Modal price " + modal.toPlainString()
							+ " is more than 10 times or less than 0.1 times the median modal " + median.toPlainString()
							+ " of " + group.size() + " records of this commodity and date"));
				}
			}
		}
		List<ValidRecord> kept = records.stream().filter((record) -> !outliers.contains(record)).toList();
		return new Split(kept, rejected);
	}

	static BigDecimal median(List<BigDecimal> sorted) {
		int size = sorted.size();
		return (size % 2 == 1) ? sorted.get(size / 2)
				: sorted.get(size / 2 - 1).add(sorted.get(size / 2)).divide(BigDecimal.valueOf(2));
	}

	private record GroupKey(UUID commodityId, LocalDate reportDate) {
	}

	/** A record rejected by V8, with the reason. */
	record Outlier(ValidRecord record, String message) {
	}

	/** The records kept, in their original order, and those rejected. */
	record Split(List<ValidRecord> kept, List<Outlier> outliers) {
	}

}

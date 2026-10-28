package com.agrinex.price.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import com.agrinex.price.SourceDataset;

/**
 * The observation selection rules of ADR-0010, as pure functions over candidate rows that already satisfy the
 * date window and the exact variety and grade filters:
 * <ol>
 * <li>both datasets are candidates; for the same market, commodity, variety, grade and report date the
 * current-dataset row takes precedence and the historical row is ignored (departure D-08);</li>
 * <li>a market's observation date is its latest report date with a candidate;</li>
 * <li>on that date the representative is the lower median of the candidates sorted by modal price, then variety
 * name, then grade name (NULL first): the element at position floor((n - 1) / 2). It is always a real
 * observation; prices are never averaged or blended.</li>
 * </ol>
 */
public final class ObservationSelection {

	private static final Comparator<String> NULLS_FIRST_CODE_POINT = Comparator.nullsFirst(String::compareTo);

	static final Comparator<ObservationRow> REPRESENTATIVE_ORDER = Comparator.comparing(ObservationRow::modalPrice)
		.thenComparing(ObservationRow::varietyName, NULLS_FIRST_CODE_POINT)
		.thenComparing(ObservationRow::gradeName, NULLS_FIRST_CODE_POINT);

	private ObservationSelection() {
	}

	/** Rule 1: drops each historical row whose identity also exists as a current-dataset row. */
	static List<ObservationRow> applyDatasetPrecedence(Collection<ObservationRow> candidates) {
		Map<ObservationRow.IdentityKey, ObservationRow> byIdentity = new LinkedHashMap<>();
		for (ObservationRow row : candidates) {
			byIdentity.merge(row.identityWithoutDataset(), row,
					(kept, other) -> (other.sourceDataset() == SourceDataset.CURRENT_PRICE) ? other : kept);
		}
		return new ArrayList<>(byIdentity.values());
	}

	/** Rule 3: the lower-median representative of the candidates of one market, commodity and date. */
	static ObservationRow representative(List<ObservationRow> candidatesOnOneDate) {
		List<ObservationRow> sorted = candidatesOnOneDate.stream().sorted(REPRESENTATIVE_ORDER).toList();
		return sorted.get((sorted.size() - 1) / 2);
	}

	/** Rules 1-3 for each market: the selected observation on its latest date with a candidate. */
	public static Map<UUID, ObservationRow> latestPerMarket(Collection<ObservationRow> candidates) {
		Map<UUID, List<ObservationRow>> byMarket = applyDatasetPrecedence(candidates).stream()
			.collect(Collectors.groupingBy(ObservationRow::marketId));
		Map<UUID, ObservationRow> selected = new LinkedHashMap<>();
		byMarket.forEach((marketId, rows) -> latestDate(rows)
			.ifPresent((date) -> selected.put(marketId, representative(rowsOn(rows, date)))));
		return selected;
	}

	/** Rule 7: the daily series, one selected real observation per date with data, ascending by date. */
	public static List<ObservationRow> dailySeries(Collection<ObservationRow> candidates) {
		Map<LocalDate, List<ObservationRow>> byDate = applyDatasetPrecedence(candidates).stream()
			.collect(Collectors.groupingBy(ObservationRow::reportDate, TreeMap::new, Collectors.toList()));
		return byDate.values().stream().map(ObservationSelection::representative).toList();
	}

	private static Optional<LocalDate> latestDate(List<ObservationRow> rows) {
		return rows.stream().map(ObservationRow::reportDate).max(Comparator.naturalOrder());
	}

	private static List<ObservationRow> rowsOn(List<ObservationRow> rows, LocalDate date) {
		return rows.stream().filter((row) -> row.reportDate().equals(date)).toList();
	}

}

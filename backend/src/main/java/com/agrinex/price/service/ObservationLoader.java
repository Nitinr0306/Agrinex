package com.agrinex.price.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.price.LoadResult;
import com.agrinex.price.ObservationCandidate;
import com.agrinex.price.domain.BusinessKeys;
import com.agrinex.price.domain.PriceObservationEntity;
import com.agrinex.price.domain.PriceObservationRevisionEntity;
import com.agrinex.price.repository.PriceObservationRepository;
import com.agrinex.price.repository.PriceObservationRevisionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads one delivery unit against the stored observations (ADR-0009 item 3), inside the transaction of the
 * calling ingestion run, so a unit is committed or rolled back as a whole (ADR-0011 item 7):
 * <ul>
 * <li>a new identity is inserted;</li>
 * <li>identical prices are a duplicate: nothing is written (departure D-07);</li>
 * <li>different prices: a revision row keeps the previous values and {@code ingested_at}, then the observation is
 * updated; its unit never changes.</li>
 * </ul>
 * The unique business key and identity constraints remain the last line of defense (spec section 31 RULE 6).
 */
@Service
class ObservationLoader {

	private static final int LOOKUP_CHUNK = 1000;

	private final PriceObservationRepository observations;

	private final PriceObservationRevisionRepository revisions;

	private final AgrinexCalendar calendar;

	ObservationLoader(PriceObservationRepository observations, PriceObservationRevisionRepository revisions,
			AgrinexCalendar calendar) {
		this.observations = observations;
		this.revisions = revisions;
		this.calendar = calendar;
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public LoadResult load(UUID ingestionRunId, UUID sourceDatasetId, List<ObservationCandidate> candidates) {
		Map<String, ObservationCandidate> byKey = keyed(sourceDatasetId, candidates);
		Map<String, PriceObservationEntity> stored = findStored(new ArrayList<>(byKey.keySet()));
		Instant now = this.calendar.now();
		List<PriceObservationEntity> inserts = new ArrayList<>();
		List<PriceObservationRevisionEntity> revisionRows = new ArrayList<>();
		int unchanged = 0;
		for (Map.Entry<String, ObservationCandidate> entry : byKey.entrySet()) {
			ObservationCandidate candidate = entry.getValue();
			PriceObservationEntity existing = stored.get(entry.getKey());
			if (existing == null) {
				inserts.add(newObservation(entry.getKey(), candidate, sourceDatasetId, ingestionRunId, now));
			}
			else if (samePrices(existing, candidate)) {
				unchanged++;
			}
			else {
				revisionRows.add(new PriceObservationRevisionEntity(existing, ingestionRunId, now));
				existing.revise(candidate.modalPrice(), candidate.minPrice(), candidate.maxPrice(), now);
			}
		}
		this.observations.saveAll(inserts);
		this.revisions.saveAll(revisionRows);
		this.observations.flush();
		return new LoadResult(inserts.size(), revisionRows.size(), unchanged);
	}

	private static Map<String, ObservationCandidate> keyed(UUID sourceDatasetId, List<ObservationCandidate> candidates) {
		Map<String, ObservationCandidate> byKey = new LinkedHashMap<>();
		for (ObservationCandidate candidate : candidates) {
			String key = BusinessKeys.of(sourceDatasetId, candidate.marketId(), candidate.commodityId(),
					candidate.varietyId(), candidate.gradeId(), candidate.reportDate());
			if (byKey.putIfAbsent(key, candidate) != null) {
				throw new IllegalArgumentException(
						"A delivery unit must be deduplicated and conflict-checked before loading (ADR-0009 item 5)");
			}
		}
		return byKey;
	}

	private Map<String, PriceObservationEntity> findStored(List<String> keys) {
		Map<String, PriceObservationEntity> stored = new HashMap<>();
		for (int start = 0; start < keys.size(); start += LOOKUP_CHUNK) {
			List<String> chunk = keys.subList(start, Math.min(keys.size(), start + LOOKUP_CHUNK));
			this.observations.findByBusinessKeyIn(chunk)
				.forEach((observation) -> stored.put(observation.getBusinessKey(), observation));
		}
		return stored;
	}

	private static PriceObservationEntity newObservation(String businessKey, ObservationCandidate candidate,
			UUID sourceDatasetId, UUID ingestionRunId, Instant now) {
		return new PriceObservationEntity(candidate.marketId(), candidate.commodityId(), candidate.varietyId(),
				candidate.gradeId(), candidate.reportDate(), candidate.modalPrice(), candidate.minPrice(),
				candidate.maxPrice(), candidate.priceUnit(), candidate.sourceCommodityName(), businessKey, sourceDatasetId,
				ingestionRunId, now);
	}

	static boolean samePrices(PriceObservationEntity stored, ObservationCandidate candidate) {
		return equal(stored.getModalPrice(), candidate.modalPrice()) && equal(stored.getMinPrice(), candidate.minPrice())
				&& equal(stored.getMaxPrice(), candidate.maxPrice());
	}

	private static boolean equal(BigDecimal a, BigDecimal b) {
		return (a == null) ? b == null : (b != null && a.compareTo(b) == 0);
	}

}

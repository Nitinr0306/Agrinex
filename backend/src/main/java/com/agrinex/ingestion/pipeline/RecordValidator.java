package com.agrinex.ingestion.pipeline;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import tools.jackson.databind.JsonNode;

import com.agrinex.commodity.CommodityInfo;
import com.agrinex.geography.DistrictInfo;
import com.agrinex.ingestion.config.IngestionProperties;
import com.agrinex.ingestion.domain.IngestionErrorCode;
import com.agrinex.ingestion.pipeline.RecordCheck.Accepted;
import com.agrinex.ingestion.pipeline.RecordCheck.OutOfScope;
import com.agrinex.ingestion.pipeline.RecordCheck.Rejected;

import org.springframework.stereotype.Component;

/**
 * Coverage and validation rules V1 to V7 of ADR-0008 item 5, applied in that order; the first failing rule
 * rejects the record. Prices are parsed as exact decimals and rounded half-up to 2 decimal places (ADR-0008 item
 * 4); an absent min or max price stays null and a zero is never read as "not reported". Dates are strictly
 * {@code dd/MM/yyyy} and never after today in Asia/Kolkata (ADR-0007).
 */
@Component
public class RecordValidator {

	static final DateTimeFormatter RECORD_DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu")
		.withResolverStyle(ResolverStyle.STRICT);

	/** V7: below Rs. 1 per kg (ADR-0008 item 5). */
	static final BigDecimal QUINTAL_PRICE_FLOOR = new BigDecimal("100.00");

	/** The largest value numeric(14,2) stores. */
	static final BigDecimal MAX_STORED_PRICE = new BigDecimal("999999999999.99");

	/** Column lengths of the reference tables the values are stored in (V1, V2 migrations). */
	static final int MAX_STATE_LENGTH = 100;

	static final int MAX_DISTRICT_LENGTH = 150;

	static final int MAX_NAME_LENGTH = 200;

	private static final Pattern DECIMAL_TEXT = Pattern.compile("-?\\d+(\\.\\d+)?");

	private final Set<String> coverageStates;

	public RecordValidator(IngestionProperties properties) {
		this.coverageStates = Set.copyOf(properties.coverageStates());
	}

	public RecordCheck check(SourceRecord record, ReferenceLookups lookups, LocalDate today) {
		if (record.state() != null && !this.coverageStates.contains(record.state())) {
			return new OutOfScope();
		}
		try {
			requirePresent(record);
			requireStorableLengths(record);
			LocalDate reportDate = reportDate(record.arrivalDate(), today);
			CommodityInfo commodity = commodity(lookups.commodity(record.commodity()), record.commodity());
			DistrictInfo district = lookups.district(record.state(), record.district())
				.orElseThrow(() -> new Rejection(IngestionErrorCode.UNKNOWN_DISTRICT,
						"District '" + record.district() + "' of state '" + record.state() + "' does not resolve"));
			BigDecimal modal = price("modal price", record.modalPrice());
			BigDecimal min = price("min price", record.minPrice());
			BigDecimal max = price("max price", record.maxPrice());
			if ((min != null && min.compareTo(modal) > 0) || (max != null && modal.compareTo(max) > 0)) {
				throw new Rejection(IngestionErrorCode.PRICE_RANGE_INVALID,
						"Prices are not ordered min <= modal <= max");
			}
			if (CommodityInfo.RUPEES_PER_QUINTAL.equals(commodity.priceUnit())
					&& modal.compareTo(QUINTAL_PRICE_FLOOR) < 0) {
				throw new Rejection(IngestionErrorCode.PRICE_BELOW_UNIT_FLOOR,
						"Modal price " + modal.toPlainString() + " is below 100.00 Rs./Quintal");
			}
			return new Accepted(new ValidRecord(record, commodity, district, reportDate, modal, min, max));
		}
		catch (Rejection rejection) {
			return new Rejected(rejection.code, rejection.getMessage());
		}
	}

	/** V1. */
	private static void requirePresent(SourceRecord record) throws Rejection {
		String missing = (record.state() == null) ? "state" : (record.district() == null) ? "district"
				: (record.market() == null) ? "market" : (record.commodity() == null) ? "commodity"
						: (record.arrivalDate() == null) ? "arrival date"
								: absent(record.modalPrice()) ? "modal price" : null;
		if (missing != null) {
			throw new Rejection(IngestionErrorCode.REQUIRED_FIELD_MISSING, "Required field " + missing
					+ " is missing or blank");
		}
	}

	private static void requireStorableLengths(SourceRecord record) throws Rejection {
		String field = tooLong(record.state(), MAX_STATE_LENGTH) ? "state"
				: tooLong(record.district(), MAX_DISTRICT_LENGTH) ? "district"
						: (tooLong(record.market(), MAX_NAME_LENGTH) || tooLong(record.marketRaw(), MAX_NAME_LENGTH))
								? "market" : tooLong(record.commodity(), MAX_NAME_LENGTH) ? "commodity"
										: tooLong(record.variety(), MAX_NAME_LENGTH) ? "variety"
												: tooLong(record.grade(), MAX_NAME_LENGTH) ? "grade" : null;
		if (field != null) {
			throw new Rejection(IngestionErrorCode.FIELD_TOO_LONG, "Field " + field + " is longer than can be stored");
		}
	}

	/** V2. */
	private static LocalDate reportDate(String text, LocalDate today) throws Rejection {
		LocalDate date;
		try {
			date = LocalDate.parse(text, RECORD_DATE);
		}
		catch (DateTimeParseException ex) {
			throw new Rejection(IngestionErrorCode.DATE_FORMAT_INVALID, "Arrival date '" + text + "' is not dd/MM/yyyy");
		}
		if (date.isAfter(today)) {
			throw new Rejection(IngestionErrorCode.DATE_IN_FUTURE, "Arrival date " + text + " is after today");
		}
		return date;
	}

	/** V3. */
	private static CommodityInfo commodity(Optional<CommodityInfo> found, String name) throws Rejection {
		CommodityInfo commodity = found.orElseThrow(() -> new Rejection(IngestionErrorCode.UNKNOWN_COMMODITY,
				"Commodity '" + name + "' is not in the known list"));
		if (commodity.priceUnit() == null) {
			throw new Rejection(IngestionErrorCode.UNIT_UNVERIFIED,
					"Commodity '" + name + "' has no verified price unit");
		}
		return commodity;
	}

	/** V5: an exact decimal, positive and storable, rounded half-up to 2 places; null when absent. */
	private static BigDecimal price(String field, JsonNode node) throws Rejection {
		if (absent(node)) {
			return null;
		}
		BigDecimal value;
		if (node.isNumber()) {
			value = node.decimalValue();
		}
		else if (node.isString() && DECIMAL_TEXT.matcher(node.stringValue().strip()).matches()) {
			value = new BigDecimal(node.stringValue().strip());
		}
		else {
			throw new Rejection(IngestionErrorCode.PRICE_NOT_NUMERIC, "The " + field + " is not a decimal number");
		}
		BigDecimal rounded = value.setScale(2, RoundingMode.HALF_UP);
		if (rounded.signum() <= 0) {
			throw new Rejection(IngestionErrorCode.PRICE_NON_POSITIVE, "The " + field + " is not greater than zero");
		}
		if (rounded.compareTo(MAX_STORED_PRICE) > 0) {
			throw new Rejection(IngestionErrorCode.PRICE_TOO_LARGE, "The " + field + " exceeds the stored precision");
		}
		return rounded;
	}

	private static boolean absent(JsonNode node) {
		return node == null || node.isNull() || (node.isString() && node.stringValue().isBlank());
	}

	private static boolean tooLong(String value, int maxLength) {
		return value != null && value.codePointCount(0, value.length()) > maxLength;
	}

	/** A rule failure inside {@link #check}; carries no stack trace. */
	private static final class Rejection extends Exception {

		private final IngestionErrorCode code;

		Rejection(IngestionErrorCode code, String message) {
			super(message, null, false, false);
			this.code = code;
		}

	}

}

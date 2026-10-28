package com.agrinex.ingestion.source;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.agrinex.price.SourceDataset;

/**
 * Field and filter names of the two data.gov.in resources, which spell them differently (evidence file section
 * 19.3). Mapping them explicitly, never by case-folding, keeps schema drift visible. The historical date filter
 * is sent as {@code dd-MM-yyyy}, as the resource metadata declares (ADR-0007 item 2); live requests on 2026-10-13
 * confirmed it returns the same records as {@code dd/MM/yyyy}.
 *
 * <p>
 * {@code sortFields} give every page request a deterministic order. Without a sort the source pages an unordered
 * result: on 2026-10-13 four offset pages of 500 for one historical report date (1,542 records) returned some
 * records twice and left 12 to 217 records out, while still adding up to the reported total, so the loss was
 * invisible to the completeness counters (ADR-0012). With these sorts the same pages returned all records, on
 * both resources. The current resource sorts its text fields only through their {@code .keyword} sub-fields (a
 * plain text field answers "Fielddata is disabled" with no records) and cannot sort {@code arrival_date}.
 */
public record SourceSchema(String state, String district, String market, String commodity, String variety,
		String grade, String arrivalDate, String minPrice, String maxPrice, String modalPrice, String stateFilter,
		String dateFilter, List<String> sortFields) {

	public static final SourceSchema CURRENT = new SourceSchema("state", "district", "market", "commodity", "variety",
			"grade", "arrival_date", "min_price", "max_price", "modal_price", "filters[state]", null,
			List.of("district.keyword", "market.keyword", "commodity.keyword", "variety.keyword", "grade.keyword",
					"min_price", "modal_price", "max_price"));

	public static final SourceSchema HISTORICAL = new SourceSchema("State", "District", "Market", "Commodity",
			"Variety", "Grade", "Arrival_Date", "Min_Price", "Max_Price", "Modal_Price", "filters[State]",
			"filters[Arrival_Date]", List.of("District", "Market", "Commodity", "Variety", "Grade", "Min_Price",
					"Modal_Price", "Max_Price"));

	/** ADR-0007 item 2. */
	public static final DateTimeFormatter DATE_FILTER_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu");

	/** The {@code sort[field]=asc} query parameters, in priority order. */
	public Map<String, String> sortParameters() {
		Map<String, String> parameters = new LinkedHashMap<>();
		this.sortFields.forEach((field) -> parameters.put("sort[" + field + "]", "asc"));
		return parameters;
	}

	public static SourceSchema of(SourceDataset dataset) {
		return (dataset == SourceDataset.CURRENT_PRICE) ? CURRENT : HISTORICAL;
	}

}

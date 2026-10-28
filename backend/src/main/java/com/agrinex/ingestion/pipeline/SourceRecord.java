package com.agrinex.ingestion.pipeline;

import tools.jackson.databind.JsonNode;

import com.agrinex.common.TextNormalizer;
import com.agrinex.ingestion.source.SourceSchema;

/**
 * One source record with its fields read by the resource's own spelling (evidence file section 19.3). Text
 * fields are trimmed and whitespace runs collapsed (ADR-0006 item 2); a missing, null, blank or non-scalar value
 * is null. {@code marketRaw} keeps the source spelling for the alias row (ADR-0014 item 4). Prices stay JSON
 * nodes until validation parses them exactly.
 */
public record SourceRecord(JsonNode payload, String state, String district, String market, String marketRaw,
		String commodity, String variety, String grade, String arrivalDate, JsonNode modalPrice, JsonNode minPrice,
		JsonNode maxPrice) {

	public static SourceRecord from(JsonNode record, SourceSchema schema) {
		String marketRaw = scalarText(record.get(schema.market()));
		return new SourceRecord(record, normalized(record, schema.state()), normalized(record, schema.district()),
				TextNormalizer.normalize(marketRaw), marketRaw, normalized(record, schema.commodity()),
				normalized(record, schema.variety()), normalized(record, schema.grade()),
				normalized(record, schema.arrivalDate()), record.get(schema.modalPrice()), record.get(schema.minPrice()),
				record.get(schema.maxPrice()));
	}

	/** The record as compact JSON text, the raw payload kept with a rejection (ADR-0008 item 5). */
	public String payloadJson() {
		return this.payload.toString();
	}

	private static String normalized(JsonNode record, String field) {
		return TextNormalizer.normalize(scalarText(record.get(field)));
	}

	private static String scalarText(JsonNode node) {
		if (node == null || node.isNull() || node.isContainer()) {
			return null;
		}
		return node.isString() ? node.stringValue() : node.asString();
	}

}

package com.agrinex.ingestion.service;

import java.util.Map;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.JsonNodeFeature;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.stereotype.Component;

/**
 * JSON text of stored payloads (ingestion_errors.raw_payload, ingestion_runs.request_scope). Numbers keep their
 * exact decimal value, so a price shown to an admin is the one the source sent (ADR-0008 item 4).
 */
@Component
public class PayloadJson {

	/** A non-JSON body, for example an HTML maintenance page, is stored up to this many characters. */
	static final int MAX_STORED_BODY_LENGTH = 65_536;

	private final JsonMapper jsonMapper = JsonMapper.builder()
		.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
		.enable(JsonNodeFeature.USE_BIG_DECIMAL_FOR_FLOATS)
		.enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
		.build();

	/** A response body as a JSON string value, cut to {@link #MAX_STORED_BODY_LENGTH} characters. */
	public String bodyAsJson(String body) {
		String bounded = (body.length() > MAX_STORED_BODY_LENGTH) ? body.substring(0, MAX_STORED_BODY_LENGTH) : body;
		return this.jsonMapper.writeValueAsString(bounded);
	}

	/** Any value as compact JSON text. */
	public String write(Object value) {
		return this.jsonMapper.writeValueAsString(value);
	}

	/** A stored JSON object as a map, with exact decimal numbers. */
	public Map<String, Object> toMap(String json) {
		return this.jsonMapper.readValue(json, new TypeReference<Map<String, Object>>() {
		});
	}

	/** Stored JSON text in compact form, the rawPayloadExcerpt format; null stays null. */
	public String compact(String json) {
		return (json != null) ? this.jsonMapper.writeValueAsString(this.jsonMapper.readTree(json)) : null;
	}

}

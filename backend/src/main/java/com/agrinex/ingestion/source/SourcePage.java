package com.agrinex.ingestion.source;

import java.util.List;

import tools.jackson.databind.JsonNode;

/**
 * One accepted page: the total the source reports for the request scope and the records it returned. Prices in
 * the records are exact decimals: they never pass through binary floating point (ADR-0008 item 4).
 */
public record SourcePage(int total, List<JsonNode> records) {
}

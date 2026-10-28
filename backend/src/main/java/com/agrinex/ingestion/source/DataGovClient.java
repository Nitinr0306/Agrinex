package com.agrinex.ingestion.source;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.cfg.JsonNodeFeature;
import tools.jackson.databind.json.JsonMapper;

import com.agrinex.ingestion.config.IngestionProperties;
import com.agrinex.ingestion.domain.IngestionErrorCode;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Server-side client of the two data.gov.in resources (spec section 31 RULE 1; ADR-0005, ADR-0011 item 6), with
 * Spring Framework's {@link RestClient} and no additional library. A page is accepted only as an HTTP 200 JSON
 * object with a records array and a total; every other outcome is a failure, retried up to the configured number
 * of times with exponential backoff, and never read as "zero records". Prices are parsed as exact decimals. The
 * API key travels as the source requires (an {@code api-key} query parameter) and is never logged: log events
 * name only the resource and the offset, and every stored text is redacted.
 */
@Component
public class DataGovClient {

	private static final Logger log = LoggerFactory.getLogger(DataGovClient.class);

	private static final String RECORDS = "records";

	private static final String TOTAL = "total";

	private final RestClient restClient;

	private final JsonMapper sourceJson;

	private final IngestionProperties properties;

	private final ApiKeyRedactor redactor;

	public DataGovClient(RestClient.Builder restClientBuilder, IngestionProperties properties,
			ApiKeyRedactor redactor) {
		this.restClient = restClientBuilder.baseUrl(properties.baseUrl())
			.requestFactory(ClientHttpRequestFactoryBuilder.jdk()
				.build(HttpClientSettings.defaults().withTimeouts(properties.connectTimeout(), properties.readTimeout())))
			.build();
		this.sourceJson = JsonMapper.builder()
			.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
			.enable(JsonNodeFeature.USE_BIG_DECIMAL_FOR_FLOATS)
			.build();
		this.properties = properties;
		this.redactor = redactor;
	}

	/**
	 * Fetches one page of a resource at an offset, retrying a failure up to {@code maxRetries} times.
	 * @param filters the {@code filters[...]} and {@code sort[...]} query parameters, already formatted
	 * @throws SourceFetchException when the last attempt fails, or when no API key is configured (nothing is sent)
	 */
	public SourcePage fetchPage(String resourceId, Map<String, String> filters, int offset) throws SourceFetchException {
		if (!this.properties.apiKeyConfigured()) {
			throw new SourceFetchException(IngestionErrorCode.API_KEY_NOT_CONFIGURED,
					"DATA_GOV_API_KEY is not configured; no request was sent", null);
		}
		SourceFetchException failure = null;
		for (int attempt = 0; attempt <= this.properties.maxRetries(); attempt++) {
			if (attempt > 0) {
				pause(this.properties.initialBackoff().multipliedBy(1L << (attempt - 1)));
			}
			try {
				return fetchOnce(resourceId, filters, offset);
			}
			catch (SourceFetchException ex) {
				failure = ex;
				log.atWarn()
					.addKeyValue("resourceId", resourceId)
					.addKeyValue("offset", offset)
					.addKeyValue("attempt", attempt + 1)
					.addKeyValue("errorCode", ex.code())
					.log("Government source page failed");
			}
		}
		throw failure;
	}

	private SourcePage fetchOnce(String resourceId, Map<String, String> filters, int offset)
			throws SourceFetchException {
		Response response;
		try {
			response = this.restClient.get()
				.uri((uri) -> {
					uri.path(resourceId)
						.queryParam("api-key", this.properties.apiKey())
						.queryParam("format", "json")
						.queryParam("limit", this.properties.pageSize())
						.queryParam("offset", offset);
					filters.forEach(uri::queryParam);
					return uri.build();
				})
				.accept(MediaType.APPLICATION_JSON)
				.exchangeForRequiredValue((request, exchange) -> new Response(exchange.getStatusCode().value(),
						StreamUtils.copyToString(exchange.getBody(), StandardCharsets.UTF_8)));
		}
		catch (ResourceAccessException ex) {
			// Also covers an I/O failure while reading the body inside the exchange.
			boolean timeout = causedBy(ex, HttpTimeoutException.class) || causedBy(ex, SocketTimeoutException.class);
			throw new SourceFetchException(timeout ? IngestionErrorCode.TIMEOUT : IngestionErrorCode.CONNECTION_FAILED,
					(timeout ? "The source did not answer in time" : "The source could not be reached") + " (offset "
							+ offset + ")",
					null);
		}
		return parse(response, offset);
	}

	private SourcePage parse(Response response, int offset) throws SourceFetchException {
		String body = this.redactor.redact(response.body());
		if (response.status() != 200) {
			throw new SourceFetchException(IngestionErrorCode.HTTP_STATUS,
					"The source answered HTTP " + response.status() + " (offset " + offset + ")", body);
		}
		JsonNode root;
		try {
			root = this.sourceJson.readTree(response.body());
		}
		catch (JacksonException ex) {
			throw new SourceFetchException(IngestionErrorCode.NON_JSON_RESPONSE,
					"The source answered HTTP 200 with a body that is not JSON (offset " + offset + ")", body);
		}
		JsonNode records = (root != null) ? root.get(RECORDS) : null;
		Integer total = (root != null) ? integer(root.get(TOTAL)) : null;
		if (root == null || !root.isObject() || records == null || !records.isArray() || total == null) {
			throw new SourceFetchException(IngestionErrorCode.UNEXPECTED_STRUCTURE,
					"The source answered HTTP 200 without a records array and a total (offset " + offset + ")", body);
		}
		List<JsonNode> items = new ArrayList<>();
		records.forEach(items::add);
		return new SourcePage(total, items);
	}

	/** The total as an integer: a JSON integer, or text holding one (limit and offset arrive as text). */
	private static Integer integer(JsonNode node) {
		if (node == null) {
			return null;
		}
		if (node.isIntegralNumber() && node.canConvertToInt()) {
			return node.intValue();
		}
		if (node.isString() && node.stringValue().strip().matches("\\d{1,9}")) {
			return Integer.valueOf(node.stringValue().strip());
		}
		return null;
	}

	private static boolean causedBy(Throwable error, Class<? extends Throwable> type) {
		for (Throwable cause = error; cause != null; cause = cause.getCause()) {
			if (type.isInstance(cause)) {
				return true;
			}
		}
		return false;
	}

	private static void pause(Duration delay) throws SourceFetchException {
		try {
			Thread.sleep(delay);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new SourceFetchException(IngestionErrorCode.CONNECTION_FAILED,
					"The page request was interrupted before its retry", null);
		}
	}

	private record Response(int status, String body) {
	}

}

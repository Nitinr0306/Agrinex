package com.agrinex.ingestion.source;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.agrinex.ingestion.config.IngestionProperties;
import com.agrinex.ingestion.domain.IngestionErrorCode;

import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * The client against a local server that answers with the shapes the live data.gov.in API returned on 2026-10-13
 * (bodies abridged; keys and record values SYNTHETIC): a page, the 403 of an unknown key, the HTTP 200 "error"
 * answer of a sort on a text field, and a body that is not JSON. A failure is never read as "zero records", and
 * the API key never appears in what is kept. The filter and sort parameters are checked in SourceQueryParametersTest.
 */
class DataGovClientTest {

	static final String SYNTHETIC_KEY = "synthetic-test-key-1111";

	HttpServer server;

	volatile int status;

	volatile String body;

	final List<Map<String, String>> requests = new CopyOnWriteArrayList<>();

	DataGovClient client;

	@BeforeEach
	void start() throws IOException {
		this.server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		this.server.createContext("/resource/", (exchange) -> {
			this.requests.add(query(exchange.getRequestURI().getRawQuery()));
			byte[] bytes = this.body.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(this.status, bytes.length);
			try (OutputStream out = exchange.getResponseBody()) {
				out.write(bytes);
			}
		});
		this.server.start();
		IngestionProperties properties = new IngestionProperties(SYNTHETIC_KEY,
				"http://" + this.server.getAddress().getHostString() + ":" + this.server.getAddress().getPort()
						+ "/resource/",
				List.of("Maharashtra"), "0 0 0 * * *", 3, 500, 1, Duration.ofMillis(5), Duration.ofSeconds(5),
				Duration.ofSeconds(5), 10);
		this.client = new DataGovClient(RestClient.builder(), properties, new ApiKeyRedactor(properties));
	}

	@AfterEach
	void stop() {
		this.server.stop(0);
	}

	@Test
	void aPageIsReadWithItsTotalAndExactDecimalPrices() throws Exception {
		answer(200, """
				{"status": "ok", "total": 71, "count": 1, "limit": "500", "offset": "0", "records": [
				 {"State": "Maharashtra", "District": "Synthetic District", "Market": "Synthetic Mandi ",
				  "Commodity": "Onion", "Commodity_Code": 23, "Variety": "Other", "Grade": "Local",
				  "Arrival_Date": "07/09/2026", "Min_Price": 1780, "Max_Price": 4640, "Modal_Price": 3963.41}]}""");

		SourcePage page = this.client.fetchPage("historical-resource",
				Map.of("filters[State]", "Maharashtra", "sort[Market]", "asc"), 0);

		assertThat(page.total()).isEqualTo(71);
		assertThat(page.records()).hasSize(1);
		assertThat(page.records().getFirst().get("Modal_Price").decimalValue()).isEqualTo(new BigDecimal("3963.41"));
		assertThat(this.requests).singleElement().satisfies((query) -> assertThat(query)
			.containsEntry("api-key", SYNTHETIC_KEY)
			.containsEntry("format", "json")
			.containsEntry("limit", "500")
			.containsEntry("offset", "0")
			.containsEntry("filters[State]", "Maharashtra")
			.containsEntry("sort[Market]", "asc"));
	}

	@Test
	void anUnauthorisedKeyIsAnHttpStatusFailureAfterTheRetriesAndTheBodyNeverHoldsTheKey() {
		answer(403, "{\"error\": \"Key not authorised\", \"echo\": \"" + SYNTHETIC_KEY + "\"}");

		assertThatExceptionOfType(SourceFetchException.class)
			.isThrownBy(() -> this.client.fetchPage("current-resource", Map.of(), 500))
			.satisfies((ex) -> {
				assertThat(ex.code()).isEqualTo(IngestionErrorCode.HTTP_STATUS);
				assertThat(ex.getMessage()).contains("403").contains("offset 500").doesNotContain(SYNTHETIC_KEY);
				assertThat(ex.responseBody()).contains("Key not authorised").doesNotContain(SYNTHETIC_KEY);
			});
		assertThat(this.requests).hasSize(2);
	}

	@Test
	void anHttp200ErrorAnswerWithoutRecordsIsAFailureNotAnEmptyPage() {
		answer(200, """
				{"status": "error", "message": "{\\"error\\":{\\"type\\":\\"search_phase_execution_exception\\",\
				\\"reason\\":\\"Fielddata is disabled on text fields by default.\\"},\\"status\\":400}"}""");

		assertThatExceptionOfType(SourceFetchException.class)
			.isThrownBy(() -> this.client.fetchPage("current-resource", Map.of("sort[market]", "asc"), 0))
			.satisfies((ex) -> assertThat(ex.code()).isEqualTo(IngestionErrorCode.UNEXPECTED_STRUCTURE));
	}

	@Test
	void aBodyThatIsNotJsonIsAFailure() {
		answer(200, "<html><body>Service Unavailable</body></html>");

		assertThatExceptionOfType(SourceFetchException.class)
			.isThrownBy(() -> this.client.fetchPage("current-resource", Map.of(), 0))
			.satisfies((ex) -> assertThat(ex.code()).isEqualTo(IngestionErrorCode.NON_JSON_RESPONSE));
	}

	private void answer(int status, String body) {
		this.status = status;
		this.body = body;
	}

	private static Map<String, String> query(String rawQuery) {
		Map<String, String> parameters = new LinkedHashMap<>();
		for (String pair : rawQuery.split("&")) {
			int equals = pair.indexOf('=');
			parameters.put(URLDecoder.decode(pair.substring(0, equals), StandardCharsets.UTF_8),
					URLDecoder.decode(pair.substring(equals + 1), StandardCharsets.UTF_8));
		}
		return parameters;
	}

}

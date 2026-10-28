package com.agrinex;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * A local stand-in for the data.gov.in resource API, serving SYNTHETIC records only, so that ingestion runs in
 * tests never reach the real source. It pages the configured records with the {@code limit} and {@code offset}
 * parameters and reports their {@code total}, the response fields the client reads.
 *
 * <p>
 * It reproduces two behaviours observed on the live API on 2026-10-13: without a {@code sort[...]} parameter the
 * order of the result is not stable between page requests (here: every request pages its own shuffle, so pages
 * overlap and records go missing while the counts still add up), and the current resource answers a sort on a
 * text field that lacks its {@code .keyword} sub-field with HTTP 200, {@code "status": "error"} and no records.
 * Like the historical resource, it applies the {@code filters[Arrival_Date]} report-date filter ({@code dd-MM-yyyy})
 * to records whose {@code Arrival_Date} is written {@code dd/MM/yyyy}.
 */
public final class GovernmentSourceStub {

	private static final String CURRENT_RESOURCE = "9ef84268-d588-465a-a308-a864a43d0070";

	/** Text fields of the current resource, sortable only through their ".keyword" sub-field. */
	private static final Set<String> CURRENT_TEXT_FIELDS = Set.of("state", "district", "market", "commodity",
			"variety", "grade", "arrival_date");

	private final HttpServer server;

	private final Map<String, List<String>> recordsByResource = new LinkedHashMap<>();

	private final List<Map<String, String>> requests = new CopyOnWriteArrayList<>();

	private Failure failure;

	private GovernmentSourceStub(HttpServer server) {
		this.server = server;
	}

	static GovernmentSourceStub start() {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
			GovernmentSourceStub stub = new GovernmentSourceStub(server);
			server.createContext("/resource/", stub::handle);
			server.start();
			return stub;
		}
		catch (IOException ex) {
			throw new IllegalStateException("The government source stub could not start", ex);
		}
	}

	String baseUrl() {
		return "http://" + this.server.getAddress().getHostString() + ":" + this.server.getAddress().getPort()
				+ "/resource/";
	}

	/** Serves these JSON record objects for the resource until replaced. */
	public synchronized void serve(String resourceId, List<String> records) {
		this.recordsByResource.put(resourceId, List.copyOf(records));
	}

	/** From now on every request fails with this status and body, until {@link #recover()}. */
	public synchronized void failWith(int status, String body) {
		this.failure = new Failure(status, body);
	}

	public synchronized void recover() {
		this.failure = null;
	}

	/** The query parameters of every request received, in order. */
	public List<Map<String, String>> requests() {
		return List.copyOf(this.requests);
	}

	private void handle(HttpExchange exchange) throws IOException {
		String resourceId = exchange.getRequestURI().getPath().substring("/resource/".length());
		Map<String, String> query = query(exchange.getRequestURI().getRawQuery());
		this.requests.add(query);
		List<String> records;
		Failure failing;
		synchronized (this) {
			records = this.recordsByResource.getOrDefault(resourceId, List.of());
			failing = this.failure;
		}
		String dateFilter = query.get("filters[Arrival_Date]");
		if (dateFilter != null) {
			String reportDate = "\"Arrival_Date\": \"" + dateFilter.replace('-', '/') + "\"";
			records = records.stream().filter((record) -> record.contains(reportDate)).toList();
		}
		if (failing != null) {
			byte[] bytes = failing.body().getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "text/html");
			exchange.sendResponseHeaders(failing.status(), bytes.length);
			try (OutputStream out = exchange.getResponseBody()) {
				out.write(bytes);
			}
			return;
		}
		int offset = Integer.parseInt(query.getOrDefault("offset", "0"));
		int limit = Integer.parseInt(query.getOrDefault("limit", "10"));
		List<String> sortFields = query.keySet().stream().filter((name) -> name.startsWith("sort[")).toList();
		byte[] body;
		if (CURRENT_RESOURCE.equals(resourceId)
				&& sortFields.stream().anyMatch((name) -> CURRENT_TEXT_FIELDS.contains(name.substring(5, name.length() - 1)))) {
			body = "{\"status\": \"error\", \"message\": \"Fielddata is disabled on text fields by default.\"}"
				.getBytes(StandardCharsets.UTF_8);
		}
		else {
			List<String> ordered = new ArrayList<>(records);
			if (sortFields.isEmpty()) {
				Collections.shuffle(ordered, new Random(this.requests.size()));
			}
			else {
				Collections.sort(ordered);
			}
			List<String> page = new ArrayList<>(ordered.subList(Math.min(offset, ordered.size()),
					Math.min(offset + limit, ordered.size())));
			body = ("{\"status\": \"ok\", \"total\": " + records.size() + ", \"count\": " + page.size()
					+ ", \"limit\": \"" + limit + "\", \"offset\": \"" + offset + "\", \"records\": ["
					+ String.join(",", page) + "]}")
				.getBytes(StandardCharsets.UTF_8);
		}
		exchange.getResponseHeaders().add("Content-Type", "application/json");
		exchange.sendResponseHeaders(200, body.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(body);
		}
	}

	private static Map<String, String> query(String rawQuery) {
		Map<String, String> parameters = new LinkedHashMap<>();
		if (rawQuery == null) {
			return parameters;
		}
		for (String pair : rawQuery.split("&")) {
			int equals = pair.indexOf('=');
			String name = URLDecoder.decode((equals < 0) ? pair : pair.substring(0, equals), StandardCharsets.UTF_8);
			String value = (equals < 0) ? "" : URLDecoder.decode(pair.substring(equals + 1), StandardCharsets.UTF_8);
			parameters.put(name, value);
		}
		return parameters;
	}

	private record Failure(int status, String body) {
	}

}

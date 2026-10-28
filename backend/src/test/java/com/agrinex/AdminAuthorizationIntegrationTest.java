package com.agrinex;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.yaml.snakeyaml.Yaml;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Server-side authorization of the admin area (spec section 28; ADR-0020), never the frontend's navigation: the
 * historical-coverage operation directly, then every admin operation of contracts/openapi.yaml, without a token and
 * with a FARMER token. A refusal is the contract's problem document and discloses nothing of what the operation
 * returns. Accounts and requests are SYNTHETIC TEST DATA.
 */
class AdminAuthorizationIntegrationTest extends IntegrationTestSupport {

	static final String COVERAGE = "/api/v1/admin/ingestion/historical-coverage";

	static final String FARMER_PASSWORD = "synthetic-pass-123";

	/** A refusal is the contract's problem document (ApiErrorResponse) and nothing else. */
	static final String[] PROBLEM_FIELDS = { "type", "title", "status", "detail", "instance", "code",
			"correlationId", "errors" };

	/** One FARMER account for the whole class: registering costs two BCrypt rounds. */
	private static String farmerToken;

	@BeforeEach
	void farmer() throws Exception {
		if (farmerToken == null) {
			String phone = syntheticPhone();
			this.mvc
				.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
					.content("{\"phoneNumber\": \"%s\", \"email\": \"%s\", \"password\": \"%s\"}".formatted(phone,
							"farmer-" + phone.substring(1) + "@agrinex.invalid", FARMER_PASSWORD)))
				.andExpect(status().isCreated());
			farmerToken = login(phone, FARMER_PASSWORD);
		}
	}

	@Test
	void theCoverageWithoutAValidTokenIsUnauthorizedAndDisclosesNothing() throws Exception {
		assertRefused(this.mvc.perform(get(COVERAGE)).andReturn().getResponse(), 401, "UNAUTHORIZED");
		assertRefused(this.mvc.perform(get(COVERAGE).header("Authorization", "Bearer not.a.token"))
			.andReturn()
			.getResponse(), 401, "UNAUTHORIZED");
	}

	@Test
	void theCoverageForAFarmerIsForbiddenAndDisclosesNothing() throws Exception {
		assertRefused(this.mvc.perform(get(COVERAGE).header("Authorization", bearer(farmerToken)))
			.andReturn()
			.getResponse(), 403, "FORBIDDEN");
	}

	@Test
	void theCoverageForAnAdminFollowsTheContract() throws Exception {
		MockHttpServletResponse response = this.mvc
			.perform(get(COVERAGE).header("Authorization", bearer(adminToken())))
			.andReturn()
			.getResponse();
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
		Map<String, Object> body = JsonPath.read(response.getContentAsString(), "$");
		assertThat(body).containsOnlyKeys("generatedAt", "coverageFrom", "coverageTo", "settleAfterDays",
				"scheduledRunsPaused", "counts", "incompleteDates", "runningRun", "nextGapCheckAt",
				"nextReconciliationAt", "maxDatesPerGapRun", "reconciliationWindowDays");
		Map<String, Integer> counts = JsonPath.read(response.getContentAsString(), "$.counts");
		assertThat(counts).containsOnlyKeys("settled", "pending", "unsettled", "failed", "missing");
		assertThat(counts.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(366);
		List<Map<String, Object>> incomplete = JsonPath.read(response.getContentAsString(), "$.incompleteDates");
		assertThat(incomplete).allSatisfy((date) -> assertThat(date).containsOnlyKeys("reportDate", "status",
				"lastAttemptAt", "lastSuccessAt", "recordsFetched", "consecutiveFailures"));
	}

	@ParameterizedTest(name = "{0} {1}")
	@MethodSource("adminOperations")
	void everyAdminOperationRefusesAMissingTokenAndAFarmer(String method, String path) throws Exception {
		assertRefused(this.mvc.perform(adminRequest(method, path)).andReturn().getResponse(), 401, "UNAUTHORIZED");
		assertRefused(this.mvc.perform(adminRequest(method, path).header("Authorization", bearer(farmerToken)))
			.andReturn()
			.getResponse(), 403, "FORBIDDEN");
	}

	/** Every operation of the contract under /api/v1/admin/, with its path variables filled in. */
	@SuppressWarnings("unchecked")
	static Stream<Arguments> adminOperations() throws IOException {
		Map<String, Object> contract;
		try (Reader reader = Files.newBufferedReader(ContractCoverageIntegrationTest.CONTRACT)) {
			contract = new Yaml().load(reader);
		}
		List<Arguments> operations = ((Map<String, Map<String, Object>>) contract.get("paths")).entrySet()
			.stream()
			.filter((path) -> path.getKey().startsWith("/api/v1/admin/"))
			.flatMap((path) -> path.getValue()
				.keySet()
				.stream()
				.filter(ContractCoverageIntegrationTest.HTTP_METHODS::contains)
				.map((method) -> Arguments.of(method.toUpperCase(Locale.ROOT), concrete(path.getKey()))))
			.toList();
		assertThat(operations).as("admin operations in the contract").hasSize(29);
		return operations.stream();
	}

	private static String concrete(String path) {
		return path.replace("{datasetType}", "HISTORICAL_PRICE").replaceAll("\\{[^}]+}", UUID.randomUUID().toString());
	}

	/** The request, with an empty JSON body where the operation takes one: authorization must come first. */
	private static MockHttpServletRequestBuilder adminRequest(String method, String path) {
		MockHttpServletRequestBuilder builder = request(HttpMethod.valueOf(method), path);
		return ("POST".equals(method) || "PUT".equals(method))
				? builder.contentType(MediaType.APPLICATION_JSON).content("{}") : builder;
	}

	private static void assertRefused(MockHttpServletResponse response, int status, String code) throws Exception {
		assertThat(response.getStatus()).as("status").isEqualTo(status);
		assertThat(response.getContentType()).startsWith("application/problem+json");
		String body = response.getContentAsString();
		Map<String, Object> problem = JsonPath.read(body, "$");
		assertThat(problem).containsOnlyKeys(PROBLEM_FIELDS);
		assertThat(problem.get("code")).isEqualTo(code);
		// No field-level detail about an operation the caller may not use, and no internals.
		assertThat((List<?>) problem.get("errors")).isEmpty();
		assertThat(body).doesNotContain("Exception").doesNotContain("at com.agrinex");
	}

}

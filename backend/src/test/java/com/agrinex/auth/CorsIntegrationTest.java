package com.agrinex.auth;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.RequestBuilder;

import com.agrinex.IntegrationTestSupport;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CORS (ADR-0017): only the public origin is allowed; a cross-origin request from any other origin is refused
 * with the contract's 403 ORIGIN_NOT_ALLOWED ApiErrorResponse, never the framework's plain text (found against
 * the running backend on 2026-10-13).
 */
class CorsIntegrationTest extends IntegrationTestSupport {

	private static final String FOREIGN_ORIGIN = "https://foreign.invalid";

	@Test
	void aForeignOriginIsRefusedWithTheContractError() throws Exception {
		for (var request : new RequestBuilder[] {
				post("/api/v1/auth/refresh").header(HttpHeaders.ORIGIN, FOREIGN_ORIGIN),
				get("/api/v1/commodities").header(HttpHeaders.ORIGIN, FOREIGN_ORIGIN) }) {
			this.mvc.perform(request)
				.andExpect(status().isForbidden())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.code").value("ORIGIN_NOT_ALLOWED"))
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.correlationId").isNotEmpty())
				.andExpect(header().exists("X-Correlation-Id"))
				.andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
				.andExpect(content().string(not(containsString("Invalid CORS request"))));
		}
	}

	@Test
	void thePublicOriginAndItsPreflightAreAllowed() throws Exception {
		this.mvc.perform(get("/api/v1/commodities").header(HttpHeaders.ORIGIN, PUBLIC_ORIGIN))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, PUBLIC_ORIGIN))
			.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
		this.mvc
			.perform(options("/api/v1/selling-options/compare").header(HttpHeaders.ORIGIN, PUBLIC_ORIGIN)
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type"))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, PUBLIC_ORIGIN));
	}

	@Test
	void aRequestWithoutAnOriginIsNotACorsRequest() throws Exception {
		this.mvc.perform(get("/api/v1/commodities")).andExpect(status().isOk());
	}

}

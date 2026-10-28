package com.agrinex.auth;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpServletResponse;

import com.agrinex.IntegrationTestSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Registration, login, refresh rotation and reuse, logout and role checks over HTTP (contract tag Auth; ADR-0016,
 * ADR-0017, ADR-0042). Accounts are SYNTHETIC test accounts on the reserved .invalid domain.
 */
class AuthFlowIntegrationTest extends IntegrationTestSupport {

	static final String PASSWORD = "synthetic-pass-123";

	static final String REFRESH_COOKIE = "__Secure-agrinex_refresh";

	@Test
	void aFarmerRegistersLogsInRotatesTheRefreshTokenAndLogsOut() throws Exception {
		String phone = register();
		MockHttpServletResponse login = loginResponse(phone, PASSWORD);
		assertThat(login.getHeader("Cache-Control")).isEqualTo("no-store");
		// MockMvc re-serializes Set-Cookie, so the attributes are checked on the parsed cookie (ADR-0042 item 3).
		MockCookie first = (MockCookie) login.getCookie(REFRESH_COOKIE);
		assertThat(first).isNotNull();
		assertThat(first.getPath()).isEqualTo("/api/v1/auth");
		assertThat(first.isHttpOnly()).isTrue();
		assertThat(first.getSecure()).isTrue();
		assertThat(first.getSameSite()).isEqualTo("Strict");
		assertThat(first.getMaxAge()).isEqualTo(604800);

		this.mvc.perform(post("/api/v1/auth/refresh").cookie(first)).andExpect(status().isForbidden());

		MockHttpServletResponse rotated = this.mvc
			.perform(post("/api/v1/auth/refresh").cookie(first).header("Origin", PUBLIC_ORIGIN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").isString())
			.andReturn()
			.getResponse();
		Cookie second = rotated.getCookie(REFRESH_COOKIE);
		assertThat(second.getValue()).isNotEqualTo(first.getValue());

		// Reuse of a rotated token revokes the whole family (ADR-0016): the newer token stops working too.
		this.mvc.perform(post("/api/v1/auth/refresh").cookie(first).header("Origin", PUBLIC_ORIGIN))
			.andExpect(status().isUnauthorized())
			.andExpect(cookie().maxAge(REFRESH_COOKIE, 0));
		this.mvc.perform(post("/api/v1/auth/refresh").cookie(second).header("Origin", PUBLIC_ORIGIN))
			.andExpect(status().isUnauthorized());

		this.mvc.perform(post("/api/v1/auth/logout").header("Origin", PUBLIC_ORIGIN))
			.andExpect(status().isNoContent());
	}

	@Test
	void aDuplicatePhoneOrEmailIsAConflict() throws Exception {
		String phone = syntheticPhone();
		String email = "farmer-" + phone.substring(1) + "@agrinex.invalid";
		this.mvc.perform(registration(phone, email)).andExpect(status().isCreated());
		this.mvc.perform(registration(phone, email.toUpperCase()))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("CONFLICT"));
	}

	@Test
	void aWrongPasswordIsRejectedWithoutSayingWhichPartWasWrong() throws Exception {
		String phone = register();
		this.mvc
			.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"identifier\": \"%s\", \"password\": \"not-the-password\"}".formatted(phone)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
	}

	@Test
	void protectedRoutesNeedAValidBearerToken() throws Exception {
		this.mvc.perform(get("/api/v1/alerts"))
			.andExpect(status().isUnauthorized())
			.andExpect(header().string("WWW-Authenticate", containsString("Bearer")));
		this.mvc.perform(get("/api/v1/alerts").header("Authorization", "Bearer not.a.token"))
			.andExpect(status().isUnauthorized())
			.andExpect(header().string("WWW-Authenticate", containsString("invalid_token")));
	}

	@Test
	void aFarmerCannotUseAdminRoutesButCanUseAlerts() throws Exception {
		String token = login(register(), PASSWORD);
		this.mvc.perform(get("/api/v1/alerts").header("Authorization", bearer(token))).andExpect(status().isOk());
		this.mvc.perform(get("/api/v1/admin/system/status").header("Authorization", bearer(token)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	void theBootstrapAdminCanReadTheSystemStatus() throws Exception {
		this.mvc.perform(get("/api/v1/admin/system/status").header("Authorization", bearer(adminToken())))
			.andExpect(status().isOk());
	}

	private String register() throws Exception {
		String phone = syntheticPhone();
		this.mvc.perform(registration(phone, "farmer-" + phone.substring(1) + "@agrinex.invalid"))
			.andExpect(status().isCreated());
		return phone;
	}

	private static org.springframework.test.web.servlet.RequestBuilder registration(String phone, String email) {
		return post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
			.content("{\"phoneNumber\": \"%s\", \"email\": \"%s\", \"password\": \"%s\"}".formatted(phone, email,
					PASSWORD));
	}

}

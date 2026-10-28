package com.agrinex;

import org.junit.jupiter.api.Test;

import com.agrinex.auth.email.EmailMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The application started with the production profile, on containers and SYNTHETIC settings (passed as test
 * properties, which are in place before StartupProfileValidator runs). The database and Redis connections come from
 * the containers; the host names below only satisfy the production rules. Scheduling is off, so nothing calls the
 * government source, and account emails are captured in memory, so nothing calls Resend.
 * <p>
 * It checks what the production profile changes: the development origin is refused, the public origin is the only
 * CORS origin, emailed links are built on the public HTTPS origin, and admin routes still require a token.
 */
@SpringBootTest(properties = { "AGRINEX_DOMAIN=agrinex.test", "AGRINEX_PUBLIC_ORIGIN=https://agrinex.test",
		"POSTGRES_HOST=agrinex-db", "POSTGRES_PORT=5432", "POSTGRES_DB=agrinex", "POSTGRES_USER=agrinex",
		"POSTGRES_PASSWORD=synthetic-database-password", "REDIS_HOST=agrinex-redis", "REDIS_PORT=6379",
		"REDIS_PASSWORD=synthetic-redis-password", "JWT_SECRET=c3ludGhldGljLXByb2QtcHJvZmlsZS10ZXN0LXNlY3JldA==",
		"DATA_GOV_API_KEY=synthetic-government-key", "AGRINEX_EMAIL_PROVIDER=resend",
		"RESEND_API_KEY=synthetic-resend-key", "AGRINEX_EMAIL_FROM=AgriNex <noreply@agrinex.test>",
		"agrinex.scheduling.enabled=false" })
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@Import({ TestcontainersConfiguration.class, CapturedEmails.class })
class ProductionProfileIntegrationTest {

	static final String PUBLIC_ORIGIN = "https://agrinex.test";

	static final String DEVELOPMENT_ORIGIN = "http://localhost:3000";

	@Autowired
	MockMvc mvc;

	@Autowired
	CapturedEmails emails;

	@Autowired
	Environment environment;

	@Test
	void onlyTheProductionProfileIsActive() {
		assertThat(this.environment.getActiveProfiles()).containsExactly("prod");
	}

	@Test
	void thePublicOriginIsTheOnlyCorsOrigin() throws Exception {
		this.mvc.perform(get("/api/v1/commodities").header(HttpHeaders.ORIGIN, DEVELOPMENT_ORIGIN))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("ORIGIN_NOT_ALLOWED"))
			.andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
		this.mvc.perform(get("/api/v1/commodities").header(HttpHeaders.ORIGIN, PUBLIC_ORIGIN))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, PUBLIC_ORIGIN));
	}

	@Test
	void anEmailedLinkIsBuiltOnThePublicHttpsOrigin() throws Exception {
		String phone = IntegrationTestSupport.syntheticPhone();
		String email = "farmer-" + phone.substring(1) + "@agrinex.invalid";
		this.mvc
			.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content("{\"phoneNumber\": \"%s\", \"email\": \"%s\", \"password\": \"synthetic-pass-123\"}"
					.formatted(phone, email)))
			.andExpect(status().isCreated());

		String text = this.emails.awaitText(email, EmailMessage.Kind.EMAIL_VERIFICATION);

		assertThat(text).contains(PUBLIC_ORIGIN + "/verify-email#token=").doesNotContain("localhost");
	}

	@Test
	void adminRoutesStillRequireAToken() throws Exception {
		this.mvc.perform(get("/api/v1/admin/ingestion/historical-coverage"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

}

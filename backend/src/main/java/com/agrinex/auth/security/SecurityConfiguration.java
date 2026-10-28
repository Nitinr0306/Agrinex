package com.agrinex.auth.security;

import java.util.List;

import com.agrinex.auth.config.OriginProperties;
import com.agrinex.auth.service.AccountResolver;
import com.agrinex.common.error.ProblemWriter;
import com.agrinex.common.logging.CorrelationId;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * The backend's security filter chain (spec section 28; ADR-0016, ADR-0017, ADR-0042):
 * <ul>
 * <li>stateless: no HTTP session, no form or basic login; the only credentials are Bearer access tokens and the
 * refresh cookie, which only the auth operations read;</li>
 * <li>CSRF tokens are not used: requests are authenticated by a header a browser never adds by itself, and the two
 * cookie-authenticated operations (refresh, logout) use a {@code SameSite=Strict} cookie and require an
 * {@code Origin} equal to the public origin (ADR-0017 items 2 and 3);</li>
 * <li>roles are enforced with {@code @PreAuthorize} on every protected operation (spec section 28), and the
 * operations that require a token are also closed to anonymous callers here, as a second line;</li>
 * <li>the admin operations are closed to every role but ADMIN here as well, before the request body is read, so a
 * non-admin receives 403 and never a validation answer about an admin request (found in the 2026-10-18
 * pre-deployment review);</li>
 * <li>CORS allows only the public origin, plus the frontend dev server in the local profile (ADR-0017 item 2).</li>
 * </ul>
 * Security-sensitive: changes need explicit human review (CLAUDE.md rule 16).
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableMethodSecurity
class SecurityConfiguration {

	@Bean
	SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, JwtDecoder accessTokenDecoder,
			AccountResolver accounts, ProblemWriter problems, OriginProperties origins) {
		ProblemAuthenticationEntryPoint entryPoint = new ProblemAuthenticationEntryPoint(problems);
		ProblemAccessDeniedHandler accessDenied = new ProblemAccessDeniedHandler(problems);
		http.csrf(AbstractHttpConfigurer::disable)
			// Uses the corsFilter bean below (HttpSecurity.cors: "If a bean by the name of corsFilter is provided,
			// that CorsFilter is used").
			.cors(Customizer.withDefaults())
			.sessionManagement((sessions) -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.requestCache(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.exceptionHandling((exceptions) -> exceptions.authenticationEntryPoint(entryPoint)
				.accessDeniedHandler(accessDenied))
			.oauth2ResourceServer((resourceServer) -> resourceServer
				.bearerTokenResolver(new ContractBearerTokenResolver())
				.authenticationEntryPoint(entryPoint)
				.accessDeniedHandler(accessDenied)
				.jwt((jwt) -> jwt.decoder(accessTokenDecoder)
					.jwtAuthenticationConverter(new AccountJwtAuthenticationConverter(accounts))))
			.authorizeHttpRequests((requests) -> requests.requestMatchers(ContractBearerTokenResolver.ADMIN)
				.hasRole("ADMIN")
				.requestMatchers(ContractBearerTokenResolver.TOKEN_REQUIRED)
				.authenticated()
				.anyRequest()
				.permitAll());
		return http.build();
	}

	/**
	 * The CORS filter of the security chain: the configuration below, with rejections written as the contract's
	 * ORIGIN_NOT_ALLOWED ApiErrorResponse (ProblemCorsProcessor).
	 */
	@Bean
	CorsFilter corsFilter(OriginProperties origins, ProblemWriter problems) {
		CorsFilter filter = new CorsFilter(corsConfigurationSource(origins));
		filter.setCorsProcessor(new ProblemCorsProcessor(problems));
		return filter;
	}

	/** The CORS filter runs inside the security chain only, never a second time as a servlet filter. */
	@Bean
	FilterRegistrationBean<CorsFilter> corsFilterServletRegistration(CorsFilter corsFilter) {
		FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(corsFilter);
		registration.setEnabled(false);
		return registration;
	}

	static CorsConfigurationSource corsConfigurationSource(OriginProperties origins) {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(origins.corsOrigins());
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
		configuration.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));
		configuration.setExposedHeaders(List.of(CorrelationId.HEADER, HttpHeaders.RETRY_AFTER, HttpHeaders.LOCATION,
				HttpHeaders.WWW_AUTHENTICATE));
		// The refresh cookie must accompany refresh and logout when the dev frontend runs on its own origin.
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/v1/**", configuration);
		return source;
	}

}

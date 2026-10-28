package com.agrinex.auth.web;

import jakarta.validation.Valid;

import com.agrinex.auth.service.LoginService;
import com.agrinex.auth.service.RegistrationService;
import com.agrinex.auth.service.SessionGrant;
import com.agrinex.auth.service.SessionService;
import com.agrinex.auth.web.dto.AuthResponse;
import com.agrinex.auth.web.dto.LoginRequest;
import com.agrinex.auth.web.dto.RegisterRequest;
import com.agrinex.common.ratelimit.RateLimited;
import com.agrinex.common.ratelimit.RateLimited.Bucket;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registration, sign-in, refresh and logout (contract tag Auth; spec section 10, 28; ADR-0016, ADR-0017,
 * ADR-0042, ADR-0047). Responses that carry tokens are never cached.
 */
@RestController
@RequestMapping("/api/v1/auth")
class SessionController {

	private final RegistrationService registrations;

	private final LoginService logins;

	private final SessionService sessions;

	private final OriginCheck originCheck;

	SessionController(RegistrationService registrations, LoginService logins, SessionService sessions,
			OriginCheck originCheck) {
		this.registrations = registrations;
		this.logins = logins;
		this.sessions = sessions;
		this.originCheck = originCheck;
	}

	@PostMapping("/register")
	@RateLimited(Bucket.REGISTER)
	ResponseEntity<AuthResponse> registerFarmer(@Valid @RequestBody RegisterRequest request) {
		return signedIn(HttpStatus.CREATED, this.registrations.register(request.toRegistration()));
	}

	@PostMapping("/login")
	@RateLimited(Bucket.LOGIN)
	ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return signedIn(HttpStatus.OK, this.logins.login(request.identifier(), request.password()));
	}

	@PostMapping("/refresh")
	@RateLimited(Bucket.REFRESH)
	ResponseEntity<AuthResponse> refreshSession(@RequestHeader(name = HttpHeaders.ORIGIN, required = false) String origin,
			@CookieValue(name = RefreshCookie.NAME, required = false) String refreshToken) {
		this.originCheck.requirePublicOrigin(origin);
		SessionGrant grant = this.sessions.refresh(refreshToken).orElseThrow(RefreshCookie::invalid);
		return signedIn(HttpStatus.OK, grant);
	}

	@PostMapping("/logout")
	ResponseEntity<Void> logout(@RequestHeader(name = HttpHeaders.ORIGIN, required = false) String origin,
			@CookieValue(name = RefreshCookie.NAME, required = false) String refreshToken) {
		this.originCheck.requirePublicOrigin(origin);
		this.sessions.logout(refreshToken);
		return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, RefreshCookie.CLEAR).build();
	}

	private static ResponseEntity<AuthResponse> signedIn(HttpStatus status, SessionGrant grant) {
		return ResponseEntity.status(status)
			.header(HttpHeaders.SET_COOKIE, RefreshCookie.set(grant.refreshToken(), grant.refreshTokenTtl()))
			.cacheControl(CacheControl.noStore())
			.body(AuthResponse.of(grant));
	}

}

package com.agrinex.auth.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.agrinex.auth.domain.Identifier;
import com.agrinex.auth.domain.UserEntity;
import com.agrinex.auth.repository.UserRepository;
import com.agrinex.auth.security.BearerChallenge;
import com.agrinex.auth.security.Passwords;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.ErrorCode;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

/**
 * Sign-in with one identifier and the account's password (ADR-0016 item 2, ADR-0047 item 3). Unknown
 * identifiers, wrong passwords and inactive accounts answer the same 401, and an unknown identifier still costs
 * one BCrypt verification, so neither the answer nor its timing reveals an account (ADR-0042, SR-15). The
 * password check runs outside any database transaction.
 */
@Service
public class LoginService {

	private final UserRepository users;

	private final Passwords passwords;

	private final LoginThrottle throttle;

	private final SessionService sessions;

	LoginService(UserRepository users, Passwords passwords, LoginThrottle throttle, SessionService sessions) {
		this.users = users;
		this.passwords = passwords;
		this.throttle = throttle;
		this.sessions = sessions;
	}

	public SessionGrant login(String rawIdentifier, String password) {
		Identifier identifier = Identifier.parse(rawIdentifier);
		this.throttle.requireNotLocked(identifier);
		Optional<UserEntity> account = this.users.findByIdentifier(identifier);
		boolean accepted;
		if (account.isPresent()) {
			accepted = this.passwords.matches(password, account.get().getPasswordHash()) && account.get().isActive();
		}
		else {
			this.passwords.verifyAgainstDummy(password);
			accepted = false;
		}
		if (!accepted) {
			this.throttle.recordFailure(identifier);
			throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "The identifier or the password is not correct.",
					List.of(), Map.of(HttpHeaders.WWW_AUTHENTICATE, BearerChallenge.REALM));
		}
		return this.sessions.open(account.get());
	}

}

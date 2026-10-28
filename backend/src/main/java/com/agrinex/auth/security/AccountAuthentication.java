package com.agrinex.auth.security;

import java.util.List;

import com.agrinex.auth.CurrentAccount;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * An authenticated request: the resolved, active account and its single role authority ({@code ROLE_FARMER},
 * {@code ROLE_BUYER} or {@code ROLE_ADMIN}; spec section 28). The access token itself is not retained.
 */
final class AccountAuthentication extends AbstractAuthenticationToken {

	private final CurrentAccount account;

	AccountAuthentication(CurrentAccount account) {
		super(List.of(new SimpleGrantedAuthority("ROLE_" + account.role().name())));
		this.account = account;
		setAuthenticated(true);
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public CurrentAccount getPrincipal() {
		return this.account;
	}

	@Override
	public String getName() {
		return this.account.userId().toString();
	}

}

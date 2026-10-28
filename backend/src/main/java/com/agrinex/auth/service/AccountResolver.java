package com.agrinex.auth.service;

import java.util.Optional;
import java.util.UUID;

import com.agrinex.auth.AuthApi;
import com.agrinex.auth.CurrentAccount;
import com.agrinex.auth.domain.FarmerProfileEntity;
import com.agrinex.auth.domain.UserEntity;
import com.agrinex.auth.repository.FarmerProfileRepository;
import com.agrinex.auth.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves the caller's account. Every operation that evaluates an access token resolves its account, and an
 * inactive or removed account is refused (ADR-0042 item 2, SR-04). Also the auth module's public interface for
 * the modules that act for the caller.
 */
@Service
@Transactional(readOnly = true)
public class AccountResolver implements AuthApi {

	private final UserRepository users;

	private final FarmerProfileRepository profiles;

	private final FarmerProfileRegistrar profileRegistrar;

	AccountResolver(UserRepository users, FarmerProfileRepository profiles, FarmerProfileRegistrar profileRegistrar) {
		this.users = users;
		this.profiles = profiles;
		this.profileRegistrar = profileRegistrar;
	}

	/** The active account with this id; empty when it is inactive or no longer exists. */
	public Optional<CurrentAccount> resolveActive(UUID userId) {
		return this.users.findById(userId)
			.filter(UserEntity::isActive)
			.map((user) -> new CurrentAccount(user.getId(), user.getRole()));
	}

	@Override
	public Optional<CurrentAccount> currentAccount() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof CurrentAccount account) {
			return Optional.of(account);
		}
		return Optional.empty();
	}

	@Override
	public Optional<UUID> findFarmerProfile(UUID userId) {
		return this.profiles.findByUserId(userId).map(FarmerProfileEntity::getId);
	}

	@Override
	public UUID requireFarmerProfile(UUID userId) {
		return findFarmerProfile(userId).orElseGet(() -> {
			try {
				return this.profileRegistrar.create(userId);
			}
			catch (DataIntegrityViolationException raced) {
				return findFarmerProfile(userId).orElseThrow(() -> raced);
			}
		});
	}

}

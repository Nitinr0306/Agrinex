package com.agrinex.auth.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.agrinex.auth.AccountAdministrationApi;
import com.agrinex.auth.OperatorRefusalException;
import com.agrinex.auth.Role;
import com.agrinex.auth.domain.AccountRules;
import com.agrinex.auth.domain.Identifier;
import com.agrinex.auth.domain.PasswordPolicy;
import com.agrinex.auth.domain.UserEntity;
import com.agrinex.auth.repository.UserRepository;
import com.agrinex.auth.security.Passwords;
import com.agrinex.common.time.AgrinexCalendar;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements the admin module's account operations (ADR-0028 item 2, ADR-0037, ADR-0041). Refusal messages are
 * shown to the operator and therefore never contain personal data.
 */
@Service
@Transactional(propagation = Propagation.MANDATORY)
class AccountAdministrationService implements AccountAdministrationApi {

	private final UserRepository users;

	private final Passwords passwords;

	private final SessionService sessions;

	private final AgrinexCalendar calendar;

	AccountAdministrationService(UserRepository users, Passwords passwords, SessionService sessions,
			AgrinexCalendar calendar) {
		this.users = users;
		this.passwords = passwords;
		this.sessions = sessions;
		this.calendar = calendar;
	}

	@Override
	public Optional<UUID> createFirstAdmin(String email, String password) {
		if (this.users.existsByRole(Role.ADMIN)) {
			return Optional.empty();
		}
		Identifier identifier = Identifier.parse(email);
		if (identifier.kind() != Identifier.Kind.EMAIL || !AccountRules.isValidEmail(identifier.value())) {
			throw new IllegalArgumentException("The bootstrap admin email is not a valid email address");
		}
		if (PasswordPolicy.violation(Role.ADMIN, password) != null) {
			throw new IllegalArgumentException("The bootstrap admin password does not meet the ADMIN password rule "
					+ "(15 to 64 characters, at most 72 bytes in UTF-8)");
		}
		UserEntity admin = UserEntity.provisionedAdmin(identifier.value(), this.passwords.hash(password),
				this.calendar.now());
		this.users.saveAndFlush(admin);
		return Optional.of(admin.getId());
	}

	@Override
	public Optional<UUID> findActiveAdmin(String email) {
		Identifier identifier = Identifier.parse(email);
		if (identifier.kind() != Identifier.Kind.EMAIL) {
			return Optional.empty();
		}
		return this.users.findByEmail(identifier.value())
			.filter((user) -> user.getRole() == Role.ADMIN && user.isActive())
			.map(UserEntity::getId);
	}

	@Override
	public UUID eraseFarmer(String phoneNumber) {
		Identifier identifier = Identifier.parse(phoneNumber);
		UserEntity farmer = ((identifier.kind() == Identifier.Kind.PHONE)
				? this.users.findByPhoneNumber(identifier.value()) : Optional.<UserEntity>empty())
			.filter((user) -> user.getRole() == Role.FARMER)
			.orElseThrow(() -> new OperatorRefusalException("No FARMER account has this phone number."));
		UUID userId = farmer.getId();
		this.users.delete(farmer);
		this.users.flush();
		return userId;
	}

	@Override
	public AccountStatusChange deactivate(String identifier) {
		UserEntity user = lock(identifier);
		if (!user.isActive()) {
			throw new OperatorRefusalException("The account is already inactive.");
		}
		if (user.getRole() == Role.ADMIN) {
			List<UserEntity> activeAdmins = this.users.lockActiveByRole(Role.ADMIN);
			if (activeAdmins.size() <= 1) {
				throw new OperatorRefusalException("The last active ADMIN cannot be deactivated.");
			}
		}
		user.deactivate();
		this.sessions.revokeAllSessions(user.getId());
		return new AccountStatusChange(user.getId(), user.getRole());
	}

	@Override
	public AccountStatusChange reactivate(String identifier) {
		UserEntity user = lock(identifier);
		if (user.isActive()) {
			throw new OperatorRefusalException("The account is already active.");
		}
		user.reactivate();
		return new AccountStatusChange(user.getId(), user.getRole());
	}

	private UserEntity lock(String rawIdentifier) {
		return this.users.findByIdentifier(Identifier.parse(rawIdentifier))
			.flatMap((user) -> this.users.findForUpdate(user.getId()))
			.orElseThrow(() -> new OperatorRefusalException("No account has this identifier."));
	}

}

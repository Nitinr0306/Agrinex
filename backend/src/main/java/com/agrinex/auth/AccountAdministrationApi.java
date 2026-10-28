package com.agrinex.auth;

import java.util.Optional;
import java.util.UUID;

/**
 * Account operations used only by the admin module (admin -> auth, ADR-0013 item 3): the first-admin bootstrap
 * (ADR-0028 item 2) and the operator procedures of ADR-0037 and ADR-0041, which have no HTTP endpoint. Each
 * operation joins the caller's transaction, so the caller's audit row commits or rolls back with it.
 */
public interface AccountAdministrationApi {

	/**
	 * Creates the first ADMIN, with its email recorded as verified (ADR-0048 item 4), when no ADMIN exists.
	 * Empty when an ADMIN already exists.
	 * @throws IllegalArgumentException when the email or the password breaks the account rules (ADR-0016)
	 */
	Optional<UUID> createFirstAdmin(String email, String password);

	/** The id of the active ADMIN with this email, the required actor of an operator procedure. */
	Optional<UUID> findActiveAdmin(String email);

	/**
	 * Deletes the FARMER account with this phone number and its cascade (ADR-0029 item 2, ADR-0037).
	 * @return the id of the erased account
	 * @throws OperatorRefusalException when no FARMER account has this phone number
	 */
	UUID eraseFarmer(String phoneNumber);

	/**
	 * Deactivates an account and revokes all its refresh-token families (ADR-0041 item 4).
	 * @throws OperatorRefusalException when the account does not exist, is already inactive, or is the last
	 * active ADMIN
	 */
	AccountStatusChange deactivate(String identifier);

	/**
	 * Reactivates an account; no token is restored (ADR-0041 item 4).
	 * @throws OperatorRefusalException when the account does not exist or is already active
	 */
	AccountStatusChange reactivate(String identifier);

	/** The account an operator procedure changed. */
	record AccountStatusChange(UUID userId, Role role) {
	}

}

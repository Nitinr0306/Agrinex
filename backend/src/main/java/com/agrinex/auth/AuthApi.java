package com.agrinex.auth;

import java.util.Optional;
import java.util.UUID;

/** Public interface of the auth module for the modules that act for the caller (alert, recommendation). */
public interface AuthApi {

	/** The caller of the current request, when a valid access token of an active account was presented. */
	Optional<CurrentAccount> currentAccount();

	/** The account's farmer profile, if it has one. */
	Optional<UUID> findFarmerProfile(UUID userId);

	/**
	 * The account's farmer profile, created on first use for an ADMIN, whose alerts are owned through a profile
	 * (ADR-0016 item 6). A FARMER always has one from registration.
	 */
	UUID requireFarmerProfile(UUID userId);

}

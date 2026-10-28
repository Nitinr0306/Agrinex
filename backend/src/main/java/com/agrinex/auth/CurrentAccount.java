package com.agrinex.auth;

import java.io.Serializable;
import java.util.UUID;

/** The authenticated caller: an active account resolved from a valid access token (ADR-0042, SR-04). */
public record CurrentAccount(UUID userId, Role role) implements Serializable {
}

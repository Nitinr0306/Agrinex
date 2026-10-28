package com.agrinex.auth.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import com.agrinex.auth.Role;
import com.agrinex.auth.domain.Identifier;
import com.agrinex.auth.domain.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository of the auth module. Only this module's code uses it (spec section 27; ADR-0013). */
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

	Optional<UserEntity> findByPhoneNumber(String phoneNumber);

	Optional<UserEntity> findByEmail(String email);

	/** The account a normalized identifier names; an unrecognized identifier names none (ADR-0047 item 3). */
	default Optional<UserEntity> findByIdentifier(Identifier identifier) {
		return switch (identifier.kind()) {
			case PHONE -> findByPhoneNumber(identifier.value());
			case EMAIL -> findByEmail(identifier.value());
			case UNRECOGNIZED -> Optional.empty();
		};
	}

	boolean existsByPhoneNumber(String phoneNumber);

	boolean existsByEmail(String email);

	boolean existsByRole(Role role);

	Optional<UserEntity> findByEmailVerificationTokenHash(String tokenHash);

	Optional<UserEntity> findByPasswordResetTokenHash(String tokenHash);

	/** Locks the account row, so concurrent changes of one account are serialized. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select u from UserEntity u where u.id = :id")
	Optional<UserEntity> findForUpdate(@Param("id") UUID id);

	/**
	 * Locks every active account of a role, so two deactivations cannot together remove the last active ADMIN
	 * (ADR-0041 item 6).
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select u from UserEntity u where u.role = :role and u.active = true order by u.id")
	List<UserEntity> lockActiveByRole(@Param("role") Role role);

	/** Daily purge of expired verification links (ADR-0048 item 11). */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update UserEntity u set u.emailVerificationTokenHash = null, u.emailVerificationExpiresAt = null
			where u.emailVerificationExpiresAt < :now
			""")
	int clearExpiredVerificationTokens(@Param("now") Instant now);

	/** Daily purge of expired reset links (ADR-0048 item 11). */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update UserEntity u set u.passwordResetTokenHash = null, u.passwordResetExpiresAt = null
			where u.passwordResetExpiresAt < :now
			""")
	int clearExpiredResetTokens(@Param("now") Instant now);

}

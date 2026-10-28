package com.agrinex.auth.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import com.agrinex.auth.domain.RefreshTokenEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository of the auth module. Only this module's code uses it (spec section 27; ADR-0013). */
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

	Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

	/** Locks the presented token for rotation, so two refreshes with one token cannot both succeed. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select t from RefreshTokenEntity t where t.tokenHash = :tokenHash")
	Optional<RefreshTokenEntity> findForRotation(@Param("tokenHash") String tokenHash);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update RefreshTokenEntity t set t.revokedAt = :now where t.familyId = :familyId and t.revokedAt is null")
	int revokeFamily(@Param("familyId") UUID familyId, @Param("now") Instant now);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update RefreshTokenEntity t set t.revokedAt = :now where t.userId = :userId and t.revokedAt is null")
	int revokeAllOfUser(@Param("userId") UUID userId, @Param("now") Instant now);

	/** Retention: tokens are deleted one day after expiry; revoked ones stay until then for reuse detection (ADR-0029). */
	@Modifying
	@Query("delete from RefreshTokenEntity t where t.expiresAt < :cutoff")
	int deleteExpiredBefore(@Param("cutoff") Instant cutoff);

}

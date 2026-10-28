package com.agrinex.transport.repository;

import java.util.Optional;
import java.util.UUID;

import com.agrinex.transport.domain.TransportAssumptionEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/** Repository of the transport module. Only this module's code uses it (spec section 27; ADR-0013). */
public interface TransportAssumptionRepository extends JpaRepository<TransportAssumptionEntity, UUID> {

	@Query("select t from TransportAssumptionEntity t where t.active = true")
	Optional<TransportAssumptionEntity> findActive();

	/** Newest first: createdAt descending, then id (API_CONVENTIONS.md section 5). */
	Page<TransportAssumptionEntity> findAllByOrderByCreatedAtDescIdAsc(Pageable pageable);

	@Modifying(flushAutomatically = true)
	@Query("update TransportAssumptionEntity t set t.active = false where t.active = true")
	int deactivateAll();

	/**
	 * Serializes concurrent version updates until the end of the transaction. Native SQL is required: an
	 * explicit table lock is the only way to make "deactivate the active version, insert the new one" safe
	 * against a concurrent update without violating the single-active-version index
	 * ({@code uq_transport_assumptions_single_active}). Reads are not blocked.
	 */
	@Modifying
	@Query(nativeQuery = true, value = "LOCK TABLE transport_assumptions IN SHARE ROW EXCLUSIVE MODE")
	void lockForVersionUpdate();

}

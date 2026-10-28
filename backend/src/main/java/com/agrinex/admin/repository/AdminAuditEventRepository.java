package com.agrinex.admin.repository;

import java.util.UUID;

import com.agrinex.admin.domain.AdminAuditEventEntity;
import com.agrinex.admin.domain.AuditAction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository of the admin module. Only this module's code uses it (spec section 27; ADR-0013). Lists are ordered
 * by the caller's {@link Pageable} sort (createdAt descending, then id).
 */
public interface AdminAuditEventRepository extends JpaRepository<AdminAuditEventEntity, UUID> {

	Page<AdminAuditEventEntity> findByAction(AuditAction action, Pageable pageable);

}

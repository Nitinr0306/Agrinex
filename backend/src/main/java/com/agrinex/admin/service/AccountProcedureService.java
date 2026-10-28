package com.agrinex.admin.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.agrinex.admin.domain.AuditAction;
import com.agrinex.admin.domain.AuditTarget;
import com.agrinex.auth.AccountAdministrationApi;
import com.agrinex.auth.AccountAdministrationApi.AccountStatusChange;
import com.agrinex.auth.OperatorRefusalException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The account operator procedures (ADR-0037, ADR-0041): erasure of a FARMER, and deactivation or reactivation of
 * any account. There is no HTTP endpoint; an operator runs them as one-off commands. Each needs an active ADMIN as
 * actor and writes one audit row in the same transaction; a refusal changes nothing and writes nothing.
 */
@Service
public class AccountProcedureService {

	/** ADR-0037 item 8: the phone number is masked to its last two digits. */
	static final int VISIBLE_PHONE_DIGITS = 2;

	private final AccountAdministrationApi accounts;

	private final AdminAuditLog audit;

	AccountProcedureService(AccountAdministrationApi accounts, AdminAuditLog audit) {
		this.accounts = accounts;
		this.audit = audit;
	}

	@Transactional
	public UUID eraseFarmer(String actorEmail, String phoneNumber, String requestReference, String reason) {
		UUID actor = activeAdmin(actorEmail);
		UUID erased = this.accounts.eraseFarmer(phoneNumber);
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("requestReference", requestReference);
		details.put("reason", reason);
		details.put("phoneNumberMasked", mask(phoneNumber));
		this.audit.record(actor, AuditAction.ACCOUNT_ERASED, AuditTarget.USER, erased, details);
		return erased;
	}

	@Transactional
	public AccountStatusChange deactivate(String actorEmail, String identifier, String reason) {
		UUID actor = activeAdmin(actorEmail);
		AccountStatusChange change = this.accounts.deactivate(identifier);
		this.audit.record(actor, AuditAction.ACCOUNT_DEACTIVATED, AuditTarget.USER, change.userId(),
				Map.of("role", change.role().name(), "reason", reason));
		return change;
	}

	@Transactional
	public AccountStatusChange reactivate(String actorEmail, String identifier, String reason) {
		UUID actor = activeAdmin(actorEmail);
		AccountStatusChange change = this.accounts.reactivate(identifier);
		this.audit.record(actor, AuditAction.ACCOUNT_REACTIVATED, AuditTarget.USER, change.userId(),
				Map.of("role", change.role().name(), "reason", reason));
		return change;
	}

	private UUID activeAdmin(String actorEmail) {
		return this.accounts.findActiveAdmin(actorEmail)
			.orElseThrow(() -> new OperatorRefusalException("The actor is not an active ADMIN account."));
	}

	/** Every character except the last two digits is replaced by an asterisk. */
	static String mask(String phoneNumber) {
		String value = phoneNumber.strip();
		int visibleFrom = Math.max(0, value.length() - VISIBLE_PHONE_DIGITS);
		return "*".repeat(visibleFrom) + value.substring(visibleFrom);
	}

}

package com.agrinex.admin.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agrinex.admin.domain.AuditAction;
import com.agrinex.admin.domain.AuditTarget;
import com.agrinex.auth.AccountAdministrationApi;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

/**
 * Creates the first ADMIN at start-up from bootstrap credentials in the deployment environment, only when no ADMIN
 * exists, and audits it as ADMIN_BOOTSTRAPPED with the new admin as actor (ADR-0028 item 2). The operator removes
 * the bootstrap password from the environment after the first start. Invalid credentials stop the start-up, so a
 * misconfiguration is never silent. Neither the email nor the password is logged or audited.
 */
@Component
@ConditionalOnWebApplication
@EnableConfigurationProperties(AdminBootstrap.BootstrapAdminProperties.class)
class AdminBootstrap implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

	private final BootstrapAdminProperties properties;

	private final AccountAdministrationApi accounts;

	private final AdminAuditLog audit;

	private final TransactionTemplate transaction;

	AdminBootstrap(BootstrapAdminProperties properties, AccountAdministrationApi accounts, AdminAuditLog audit,
			TransactionTemplate transaction) {
		this.properties = properties;
		this.accounts = accounts;
		this.audit = audit;
		this.transaction = transaction;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!StringUtils.hasText(this.properties.email()) || !StringUtils.hasText(this.properties.password())) {
			return;
		}
		Optional<UUID> created = this.transaction.execute((status) -> {
			Optional<UUID> admin = this.accounts.createFirstAdmin(this.properties.email(), this.properties.password());
			admin.ifPresent((adminId) -> this.audit.record(adminId, AuditAction.ADMIN_BOOTSTRAPPED, AuditTarget.USER,
					adminId, Map.of("reason", "First ADMIN created from the deployment environment (ADR-0028 item 2)")));
			return admin;
		});
		if (created != null && created.isPresent()) {
			log.atWarn().log("First ADMIN created; remove AGRINEX_BOOTSTRAP_ADMIN_PASSWORD from the environment");
		}
		else {
			log.atWarn().log("Bootstrap admin credentials are set but an ADMIN exists; remove them from the environment");
		}
	}

	/** {@code agrinex.bootstrap-admin}: the first ADMIN's email and password, used once (ADR-0028 item 2). */
	@ConfigurationProperties("agrinex.bootstrap-admin")
	record BootstrapAdminProperties(String email, String password) {

		@Override
		public String toString() {
			return "BootstrapAdminProperties[]";
		}

	}

}

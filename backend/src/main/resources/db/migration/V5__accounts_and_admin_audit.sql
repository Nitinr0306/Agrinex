-- V5: accounts, farmer profiles, refresh tokens and the admin audit log.
-- Owner modules: auth (users, farmer_profiles, refresh_tokens); admin (admin_audit_events).
-- Design: docs/database/DATA_DICTIONARY.md and INDEXES_AND_CONSTRAINTS.md (spec section 20 and 28;
-- ADR-0016, ADR-0020, ADR-0029, ADR-0037, ADR-0041, ADR-0047, ADR-0048). Credentials are stored only as
-- BCrypt or SHA-256 hashes; no secret is ever stored in clear.

CREATE TABLE users (
    id                            uuid         NOT NULL DEFAULT gen_random_uuid(),
    phone_number                  varchar(20),
    email                         varchar(254),
    password_hash                 varchar(100) NOT NULL,
    role                          varchar(10)  NOT NULL,
    is_active                     boolean      NOT NULL DEFAULT true,
    created_at                    timestamptz  NOT NULL DEFAULT now(),
    email_verified_at             timestamptz,
    email_verification_token_hash varchar(64),
    email_verification_expires_at timestamptz,
    password_reset_token_hash     varchar(64),
    password_reset_expires_at     timestamptz,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_phone_number UNIQUE (phone_number),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_email_verification_token_hash UNIQUE (email_verification_token_hash),
    CONSTRAINT uq_users_password_reset_token_hash UNIQUE (password_reset_token_hash),
    CONSTRAINT ck_users_role CHECK (role IN ('FARMER', 'BUYER', 'ADMIN')),
    CONSTRAINT ck_users_role_identifier CHECK ((phone_number IS NOT NULL OR email IS NOT NULL) AND (role <> 'FARMER' OR (phone_number IS NOT NULL AND email IS NOT NULL)) AND (role <> 'ADMIN' OR email IS NOT NULL)),
    CONSTRAINT ck_users_phone_format CHECK (phone_number IS NULL OR phone_number ~ '^\+91[6-9][0-9]{9}$'),
    CONSTRAINT ck_users_email_format CHECK (email IS NULL OR (email = lower(email) AND email ~ '^[^@\s]+@[^@\s]+$')),
    CONSTRAINT ck_users_email_verified CHECK (email_verified_at IS NULL OR email IS NOT NULL),
    CONSTRAINT ck_users_email_verification_token CHECK ((email_verification_token_hash IS NULL) = (email_verification_expires_at IS NULL) AND (email_verification_token_hash IS NULL OR (email_verification_token_hash ~ '^[0-9a-f]{64}$' AND email_verified_at IS NULL))),
    CONSTRAINT ck_users_password_reset_token CHECK ((password_reset_token_hash IS NULL) = (password_reset_expires_at IS NULL) AND (password_reset_token_hash IS NULL OR (password_reset_token_hash ~ '^[0-9a-f]{64}$' AND email_verified_at IS NOT NULL)))
);

CREATE TABLE farmer_profiles (
    id                  uuid         NOT NULL DEFAULT gen_random_uuid(),
    user_id             uuid         NOT NULL,
    display_name        varchar(100),
    default_district_id uuid,
    preferred_locale    varchar(5),
    CONSTRAINT pk_farmer_profiles PRIMARY KEY (id),
    CONSTRAINT fk_farmer_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_farmer_profiles_default_district FOREIGN KEY (default_district_id) REFERENCES districts (id) ON DELETE RESTRICT,
    CONSTRAINT uq_farmer_profiles_user UNIQUE (user_id),
    CONSTRAINT ck_farmer_profiles_locale CHECK (preferred_locale IS NULL OR preferred_locale IN ('mr', 'hi', 'en'))
);

CREATE TABLE refresh_tokens (
    id         uuid        NOT NULL DEFAULT gen_random_uuid(),
    user_id    uuid        NOT NULL,
    token_hash varchar(64) NOT NULL,
    expires_at timestamptz NOT NULL,
    family_id  uuid        NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    revoked_at timestamptz,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT ck_refresh_tokens_hash_format CHECK (token_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_refresh_tokens_times CHECK (expires_at > created_at AND (revoked_at IS NULL OR revoked_at >= created_at))
);

CREATE INDEX ix_refresh_tokens_family ON refresh_tokens (family_id);

CREATE TABLE admin_audit_events (
    id             uuid         NOT NULL DEFAULT gen_random_uuid(),
    actor_user_id  uuid         NOT NULL,
    action         varchar(50)  NOT NULL,
    target_type    varchar(50)  NOT NULL,
    target_id      varchar(100),
    details        jsonb        NOT NULL DEFAULT '{}'::jsonb,
    correlation_id varchar(100),
    created_at     timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT pk_admin_audit_events PRIMARY KEY (id),
    CONSTRAINT fk_admin_audit_events_actor FOREIGN KEY (actor_user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_admin_audit_events_action CHECK (action IN ('INGESTION_TRIGGERED', 'INGESTION_PAUSED', 'INGESTION_RESUMED', 'TRANSPORT_ASSUMPTIONS_UPDATED', 'RECOMMENDATION_WEIGHTS_UPDATED', 'MARKET_VERIFIED', 'MARKET_MERGED', 'MARKET_DEACTIVATED', 'MARKET_REACTIVATED', 'MARKET_COORDINATES_SET', 'COMMODITY_ADDED', 'COMMODITY_UNIT_VERIFIED', 'COMMODITY_NAMES_UPDATED', 'DISTRICT_ALIAS_ADDED', 'ADMIN_BOOTSTRAPPED', 'ACCOUNT_ERASED', 'ACCOUNT_DEACTIVATED', 'ACCOUNT_REACTIVATED')),
    CONSTRAINT ck_admin_audit_events_target_type CHECK (target_type <> ''),
    CONSTRAINT ck_admin_audit_events_no_credentials CHECK (NOT (details ?| ARRAY['password', 'password_hash', 'api-key', 'api_key', 'apikey', 'secret', 'token', 'refresh_token']))
);

CREATE INDEX ix_admin_audit_events_created_at ON admin_audit_events (created_at);
CREATE INDEX ix_admin_audit_events_target ON admin_audit_events (target_type, target_id);

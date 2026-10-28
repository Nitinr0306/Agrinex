# AgriNex — Deployment and Operations Guide

**Status:** REFERENCE (2026-10-18). The runbook of the public deployment decided in ADR-0055: one Oracle Cloud Always Free VM running the root `docker-compose.yml` (ADR-0021), at `https://agrinex.managio.me`. Section 12 records what has actually been deployed and verified; nothing in the other sections is a claim that a step has run.

## 1. What runs where

| Component | Where | Notes |
|---|---|---|
| Nginx 1.30.5 | Container `agrinex-nginx`, ports 80 and 443 | The only published ports. TLS, routing and headers (ADR-0017); configuration `infra/nginx/templates/default.conf.template` |
| Frontend (Next.js 16, standalone server) | Container `agrinex-frontend`, port 3000, internal | Image from `frontend/Dockerfile`; mock mode off; no secret |
| Backend (Spring Boot 4.1, Java 25) | Container `agrinex-backend`, port 8080, internal | Image from `backend/Dockerfile`; profile `prod`; runs Flyway, the ingestion schedule and the historical maintenance (ADR-0011, ADR-0054) |
| PostgreSQL 16.15 | Container `agrinex-db`, internal | Named volume `agrinex_agrinex-pgdata` |
| Redis 7.4.11 | Container `agrinex-redis`, internal | No persistence, password, 128 MB (ADR-0018) |
| certbot | On the host | Certificates in `/etc/letsencrypt`, HTTP-01 webroot `/var/www/certbot`, renewal by the package's timer with a deploy hook (ADR-0021 item 4) |
| Backups | On the host | `scripts/backup_db.sh` by cron at 02:30 IST, 7 days in `/var/backups/agrinex` (ADR-0021 item 6) |

The optional ML job (ADR-0030) is not deployed: spec §35 criteria and `ML_CONTRACT_BOUNDARY.md` §8 still forbid it, and the `agrinex_ml` role stays without login.

## 2. Host

- **Provider and plan:** Oracle Cloud Infrastructure, Always Free tier (ADR-0055). Cost: zero while the account is not upgraded.
- **VM:** VM.Standard.A1.Flex (Arm64), 2 OCPU, 12 GB memory, 100 GB boot volume, Canonical Ubuntu 24.04, home region India West (Mumbai) or India South (Hyderabad).
- **Account:** NitinR0306@gmail.com, owned and operated by the project owner.
- **Network:** the VCN security list allows TCP 22, 80 and 443 inbound; the host firewall allows 80 and 443 (`infra/deploy/setup_host.sh`).
- **Limitations of the free tier** (Oracle "Always Free Resources", verified 2026-10-18):
  - no availability guarantee;
  - Oracle may reclaim an instance that is idle for 7 days (95th-percentile CPU, network and memory all below 20%);
  - Ampere capacity can be temporarily unavailable when creating or resizing;
  - backups are on the same VM unless copied off it (section 7).

## 3. Owner actions (one time)

These need the owner's accounts; Claude does not perform them, and never claims they are done until the owner confirms.

1. **Oracle Cloud account and VM** (section 2), adding the deployment SSH public key. Restricting TCP 22 in the VCN security list to the owner's IP address is recommended.
2. **Open TCP 80 and 443** in the VCN security list.
3. **DNS:** the A record `agrinex` → the VM's public IPv4 address, at the DNS provider of `managio.me`.
4. **Government and Resend keys: deferred by the owner (2026-10-18).** The first deployment uses the existing keys; replacements follow the procedure of section 9 after deployment. This carries a residual risk, stated in section 11:
   - the data.gov.in key (`DATA_GOV_API_KEY`) was exposed before deployment (`docs/security/TASK_12_SECURITY_REVIEW.md` §7), and it is not safe;
   - the existing Resend key has full access to the Resend account, not sending access only.
5. **Write the owner file** `C:\Users\DELL\.agrinex-secrets\prod.env`, outside the repository and never pasted into chat. It holds three lines, whose values are the current keys and sender until they are replaced:
   ```
   DATA_GOV_API_KEY=<key>
   RESEND_API_KEY=<key>
   AGRINEX_EMAIL_FROM=<sender on managio.me, as "address" or "Name <address>">
   ```
   `*.env` files are ignored by Git and by both image build contexts.

## 4. First deployment

All server commands run on the VM as `ubuntu`, from `/opt/agrinex`, unless stated otherwise.

### 4.1 Host

`bash infra/deploy/setup_host.sh` once, then log out and in (Docker, certbot, host firewall for 80 and 443, the directories).

### 4.2 Source bundle (from the development machine)

1. `python infra/deploy/build_source_bundle.py <folder outside the repository>`. It:
   - selects files from an explicit allow-list: the image sources, `docker-compose.yml`, the Nginx templates, `infra/deploy/`, the health and backup scripts and this guide;
   - never selects Git-ignored files: `.env` files, `.mcp.json`, `local-reference-data/`, build output;
   - drops tests, `e2e/`, dumps and keys;
   - refuses to write anything if a selected file contains a secret value of `infra/.env` or `.mcp.json`, or a token pattern;
   - writes `agrinex-source.tgz` and `MANIFEST.sha256` (path, SHA-256). On 2026-10-18: 617 files, 0.5 MB.
2. `scp -i ~/.ssh/agrinex_oci agrinex-source.tgz ubuntu@<vm>:/tmp/`.
3. On the VM: `tar -xzf /tmp/agrinex-source.tgz -C /opt/agrinex && cd /opt/agrinex && sha256sum -c --quiet MANIFEST.sha256`.

### 4.3 Environment file

1. `scp -i ~/.ssh/agrinex_oci C:\Users\DELL\.agrinex-secrets\prod.env ubuntu@<vm>:/tmp/prod.env`.
2. On the VM: `bash infra/deploy/create_server_env.sh /tmp/prod.env`. The script:
   - creates `/opt/agrinex/.env` from `infra/deploy/production.env.example`, with mode 600;
   - generates `POSTGRES_PASSWORD`, `REDIS_PASSWORD` and `JWT_SECRET` with `openssl` on the server, so they differ from every local value;
   - takes the three owner values from the uploaded file, then deletes it;
   - never displays a value, and refuses to overwrite an existing `.env`.
3. The profile (`prod`), the public origin (`https://` + `AGRINEX_DOMAIN`) and the email provider (`resend`) are pinned in `docker-compose.yml`, not in `.env`.
4. `AGRINEX_BOOTSTRAP_ADMIN_*` stay empty, because the production data holds the ADMIN account (section 4.5).

### 4.4 Certificate

Before Nginx exists, with port 80 free:

```sh
sudo certbot certonly --standalone -d agrinex.managio.me --agree-tos -m <owner email> --no-eff-email
sudo certbot reconfigure --cert-name agrinex.managio.me --webroot --webroot-path /var/www/certbot
sudo install -m 755 infra/deploy/certbot-deploy-hook.sh /etc/letsencrypt/renewal-hooks/deploy/agrinex-reload-nginx.sh
```

Renewals then use the webroot that Nginx serves, and the hook reloads Nginx. After the stack runs, `sudo certbot renew --dry-run` must succeed.

### 4.5 Database seed (ADR-0055 item 3)

The curated copy `agrinex_prod_seed.dump` was made from the local database without FARMER accounts, sessions, alerts or recorded comparisons. It is kept on the development machine until the restore **and** one backup taken on the server have been verified.

```sh
scp -i ~/.ssh/agrinex_oci agrinex_prod_seed.dump ubuntu@<vm>:/tmp/        # from the development machine
docker compose up -d agrinex-db
docker compose exec -T agrinex-db psql -U agrinex -d agrinex -c "CREATE ROLE agrinex_ml NOLOGIN"
docker compose exec -T agrinex-db pg_restore -U agrinex -d agrinex --no-owner --exit-on-error < /tmp/agrinex_prod_seed.dump
```

The role mirrors migration V9, which the restored Flyway history does not re-run. After the restore is verified, `shred -u /tmp/agrinex_prod_seed.dump`.

### 4.6 Start, verify, back up

1. `docker compose up -d --build`. The backend checks its production settings before anything else (section 5). It then applies V13 (the report-date ledger, seeded from the copied runs) and starts its schedules. Nginx starts after the backend and frontend are healthy.
2. `scripts/health_check.sh https://agrinex.managio.me` (section 8).
3. **Backups:** `crontab -e` and add the line below. 21:00 UTC is 02:30 IST, between the ingestion runs at 00:00 and 06:00 IST.
   ```
   0 21 * * * cd /opt/agrinex && scripts/backup_db.sh >> /var/backups/agrinex/backup.log 2>&1
   ```
   Run `scripts/backup_db.sh` once by hand, and copy that backup off the VM (section 7).
4. **Admin password (ADR-0028):**
   - The production ADMIN account comes from the curated copy. On 2026-10-18 its stored password still matched the one kept in plain text in local `infra/.env`.
   - Right after the first production sign-in, the owner changes it with the password-reset flow at `https://agrinex.managio.me/forgot-password`. The reset email goes to the ADMIN's own address.
   - The bootstrap variables are never set on the server, so ADR-0028's removal requirement holds from the start (`create_server_env.sh` leaves them empty; `set_env_value.sh --clear` empties them if they were ever set).
   - Locally, `infra/.env` keeps the bootstrap password for the browser journeys. It is never copied to the server.
5. **Email:** one live account email from production must arrive, with its link on `https://agrinex.managio.me` (gate AR-07).

## 5. Configuration

### 5.1 Which file is used where

| Environment | File | Created from | Who creates it |
|---|---|---|---|
| Local development (sections 4–6 of the local guide, `infra/docker-compose.yml`, `infra/docker-compose.dev.yml`) | `infra/.env` on the developer machine | `infra/.env.example` | The developer |
| Public deployment | `/opt/agrinex/.env` on the VM, mode 600, owner `ubuntu` | `infra/deploy/production.env.example` | `infra/deploy/create_server_env.sh`, once |
| Automated tests | none | test properties (`IntegrationTestSupport`) | — |

- The frontend has no environment file: it needs no variable in production (it calls `/api/v1/` on its own origin), and its two development variables are set in the terminal (`AGRINEX_DEV_API_ORIGIN`, and `NEXT_PUBLIC_USE_MOCK_API` for mock mode).
- The production template is named `production.env.example`, not `.env.production.example`, deliberately. `.gitignore` ignores every `.env.*` file except `.env.example`. A file named `.env.production.example` would therefore be invisible to Git, and also to `build_source_bundle.py`, which never selects Git-ignored files, so it would never reach the server.

### 5.2 Variable reference (production)

| Variable | Purpose | Value comes from | Mandatory |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Selects the backend profile | Pinned to `prod` in `docker-compose.yml`; never read from `.env` | Yes (fixed) |
| `AGRINEX_DOMAIN` | The public host name: Nginx server name and certificate path; the backend checks the public origin against it | `.env` (`agrinex.managio.me`, from the template) | Yes |
| `AGRINEX_PUBLIC_ORIGIN` | CORS, the Origin check on refresh and logout, and the base of every emailed link | Derived in `docker-compose.yml` as `https://` + `AGRINEX_DOMAIN` | Yes (derived) |
| `POSTGRES_DB`, `POSTGRES_USER` | Database name and owner | `.env` (`agrinex`, from the template) | Yes |
| `POSTGRES_PASSWORD` | Database password | Generated on the server (`openssl rand -hex 32`) | Yes |
| `POSTGRES_HOST`, `POSTGRES_PORT` | Where the backend reaches the database | Pinned in `docker-compose.yml` (`agrinex-db`, `5432`) | Yes (fixed) |
| `REDIS_PASSWORD` | Redis password | Generated on the server (`openssl rand -hex 32`) | Yes |
| `REDIS_HOST`, `REDIS_PORT` | Where the backend reaches Redis | Pinned in `docker-compose.yml` (`agrinex-redis`, `6379`) | Yes (fixed) |
| `REDIS_MAXMEMORY` | Redis memory limit | `.env` (`128mb`, from the template) | No (default 128mb) |
| `JWT_SECRET` | Signing key of access tokens (HS256, at least 256 bits) | Generated on the server (`openssl rand -base64 32`) | Yes |
| `DATA_GOV_API_KEY` | data.gov.in key for ingestion; backend only, never the browser | Owner's `prod.env` | Yes |
| `AGRINEX_EMAIL_PROVIDER` | Account email delivery | Pinned to `resend` in `docker-compose.yml` | Yes (fixed) |
| `RESEND_API_KEY` | Resend key for account email; backend only | Owner's `prod.env` | Yes |
| `AGRINEX_EMAIL_FROM` | Sender address on the Resend-verified domain `managio.me` | Owner's `prod.env` | Yes |
| `AGRINEX_BOOTSTRAP_ADMIN_EMAIL`, `AGRINEX_BOOTSTRAP_ADMIN_PASSWORD` | Creates the first ADMIN of an empty database only (ADR-0028) | Empty: the production data already has the ADMIN account | No; must stay empty after the first start |

A missing or invalid mandatory value stops the backend before it does anything, with a message naming the variable (section 5.3). The government and Resend keys are read only by the backend (`agrinex.ingestion.api-key`, `agrinex.email.resend-api-key` in `application.yml`). They are not in any source file, image or frontend bundle; the 2026-10-18 scan of the source bundle and the image layers found neither.

### 5.3 Profiles

The backend has three profiles. Exactly one is selected, or the backend refuses to start (`StartupProfileValidator`, checked before the application context exists):

| | `local` | `prod` | `test` |
|---|---|---|---|
| Used by | Development: guide sections 4–6, `infra/docker-compose.dev.yml`. Its former name `dev` still selects it | The public deployment only; pinned by `docker-compose.yml` | Automated tests (`IntegrationTestSupport`) |
| Configuration (`application.yml`, the only configuration file) | The `local` document: localhost defaults; CORS also allows `http://localhost:3000` | The `prod` document: every value required, no defaults | Shared document, test properties and containers |
| Public origin and email links | `http://localhost:3000` (or `AGRINEX_PUBLIC_ORIGIN`) | `https://` + `AGRINEX_DOMAIN`; every emailed link starts with it (`AuthEmails`) | Test value |
| Startup checks | Profile exclusivity | Before any bean, connection or migration, the start stops unless every check below passes. The failure names the variables, never their values | Profile exclusivity |

Production checks:
- an https public origin equal to `https://` + `AGRINEX_DOMAIN`;
- database and Redis hosts that are not this machine, with their user and passwords;
- a JWT secret of at least 256 bits, in base64;
- the government key, sent over https;
- Resend, with its key and a sender address;
- no extra CORS origin;
- never `prod` together with another profile.

Where each value belongs:

| Value | Local | Production |
|---|---|---|
| Database, Redis and JWT secrets | `infra/.env` | Generated on the server into `/opt/agrinex/.env` (section 4.3) |
| `DATA_GOV_API_KEY` | `infra/.env` (the key exposed before deployment, until rotated) | Rotated key, from the owner's `prod.env` |
| `RESEND_API_KEY` | `infra/.env` (full-access key) | New sending-only key, from the owner's `prod.env` |
| `AGRINEX_EMAIL_FROM` | `infra/.env` | From the owner's `prod.env` (the verified managio.me sender) |
| `AGRINEX_DOMAIN` | Not used | `/opt/agrinex/.env` (`agrinex.managio.me`, from the template) |
| Profile, public origin, email provider, service hosts and ports | `infra/.env` / the `local` document of `application.yml` | `docker-compose.yml` (pinned) |

- **Frontend:** the browser calls `/api/v1/` on the same origin through Nginx, so no frontend URL needs configuring. The only `NEXT_PUBLIC_*` value is the mock flag, set to `false` in the image; `next.config.ts` refuses to build the image with it set to `true`.
- **Secrets:** never in images, logs, the repository or frontend code. Containers receive only the variables listed for them in `docker-compose.yml`. `docker compose config` and `docker inspect` show secret values to anyone in the server's `docker` group, so neither is run where others can see the screen.

## 6. Scheduled work (Asia/Kolkata)

| Time | Job | Source |
|---|---|---|
| 00:00, 06:00, 12:00, 18:00 | CURRENT_PRICE run, then HISTORICAL_PRICE over today − 3 … today | ADR-0011 item 1 |
| At backend start | CURRENT_PRICE run, unless one loaded data in the last 6 hours | ADR-0011 item 2 |
| 03:00 daily | Gap check: one HISTORICAL run over the newest incomplete report dates, at most 31 | ADR-0054 item 3 |
| 03:30 daily | Retention purges: ingestion errors 180 days, forecasts 90 days, alert events 180 days, expired tokens | ADR-0029 |
| 04:00 Sunday | Reconciliation: one HISTORICAL run over today − 14 … today − 1 | ADR-0054 item 4 |
| 02:30 daily (host cron) | Database backup, 7 days kept | ADR-0021 item 6 |

The in-process scheduler needs exactly one backend instance (`DEPLOYMENT_ARCHITECTURE.md` A-02). An admin pauses a dataset's scheduled runs, the maintenance runs included, from *Admin → Data health*; manual triggers keep working.

## 7. Backup and recovery

- **Backup:** `scripts/backup_db.sh` writes `/var/backups/agrinex/agrinex-<UTC time>.dump` (custom format) and removes dumps older than 7 days. It never touches the database.
- **Off-VM copy (owner):** from the development machine, `scp -i ~/.ssh/agrinex_oci ubuntu@<vm>:/var/backups/agrinex/<file>.dump <local folder>`. Without it, losing the VM loses the backups too.
- **Restore** (into an empty database; never over production data without the owner's approval): stop the backend (`docker compose stop agrinex-backend agrinex-nginx`), create an empty database, `pg_restore --no-owner --exit-on-error`, start again.
- **Data that can be fetched again:** prices come from data.gov.in, which held Maharashtra data back to at least 2022 on 2026-10-18; the gap check refills missing report dates within the last 366 days. Curation (market verification and merges), accounts and audit events exist only in the database and its backups.

## 8. Verification and maintenance

- **Quick check:** `scripts/health_check.sh https://agrinex.managio.me`. It checks:
  - the container health;
  - backend readiness inside the network;
  - the HTTP redirect, home page, `/api/health`, a public API call, `/actuator/` blocked, admin API protected;
  - the security headers.
- **Logs:** `docker compose logs --since 1h agrinex-backend` (structured JSON; no secrets, bodies or personal data, ADR-0042). Docker keeps 5 × 10 MB per container.
- **Redeploy after a code change:** build a new bundle (section 4.2), copy and unpack it over `/opt/agrinex` (the `.env` file is not in the bundle and stays), then `docker compose up -d --build`. Flyway applies new migrations at start; the database volume is kept.
- **Rollback:** keep the previous images by tagging them before a rebuild (`docker tag agrinex-backend:local agrinex-backend:previous`). To roll back, retag and run `docker compose up -d --no-build`. A migration is never rolled back by deleting it; a fix is a new migration.
- **HSTS:** the edge starts with `max-age=86400`, one day (pre-deployment review R6). After a week of stable HTTPS, change it to `31536000` in `infra/nginx/templates/default.conf.template` and run `docker compose up -d --no-deps --force-recreate agrinex-nginx`. Nginx renders the template only when its container starts, so `nginx -s reload` is not enough.
- **Fixed internal addresses:** Nginx 172.30.0.10, backend .20, frontend .21. Recreating one service never leaves Nginx pointing at an old address; Docker assigns other containers from .128 upward.
- **Never:** `docker compose down -v` (it deletes the database volume), or printing `.env`.

## 9. Credential updates

The government key, the Resend key and the sender are replaced without a redeployment. **Changing the configured value does not revoke the previous key.** Revoking it is a separate step at the provider (step 6).

1. **Generate the replacement at the provider:**
   - data.gov.in: sign in at https://data.gov.in → **My Account** → **API Key** → **Generate Your New API KEY**.
   - Resend: https://resend.com/api-keys → **Create API Key** → permission **Sending access**, domain `managio.me`. A sending-only key restricted to the verified domain is preferred: it cannot read or change anything else in the account.
2. **Store it on the server, nowhere else:**
   ```sh
   ssh -i ~/.ssh/agrinex_oci ubuntu@<vm>
   cd /opt/agrinex && bash infra/deploy/set_env_value.sh DATA_GOV_API_KEY      # or RESEND_API_KEY, AGRINEX_EMAIL_FROM
   ```
   The value is typed at a hidden prompt. The script rewrites `.env` atomically, keeps mode 600 and never displays the value. It refuses the generated database, Redis and JWT secrets.
3. **Recreate the backend only:** `docker compose up -d --no-deps --force-recreate agrinex-backend`.
   - The database, Redis, frontend and Nginx keep running.
   - The backend keeps its fixed address, so Nginx keeps reaching it.
   - The start-up checks run again first; a mistake stops the backend with a message naming the variable.
4. **Check that the new key works, without displaying it.** Each command prints only an HTTP status.
   - Government key (200 means accepted):
     ```sh
     docker compose exec -T agrinex-backend sh -c 'curl -s -o /dev/null -w "%{http_code}\n" -A agrinex-check "https://api.data.gov.in/resource/9ef84268-d588-465a-a308-a864a43d0070?format=json&limit=1&api-key=$DATA_GOV_API_KEY"'
     ```
   - Resend key: a sending-only key answers 401 `restricted_api_key` on this read call, which shows that the key exists and is restricted. A full-access key answers 200.
     ```sh
     docker compose exec -T agrinex-backend sh -c 'curl -s -w "\n%{http_code}\n" -H "Authorization: Bearer $RESEND_API_KEY" https://api.resend.com/domains | grep -o "\"name\":\"[a-z_]*\"\|^[0-9]*$"'
     ```
5. **Confirm the real flow:**
   - **Ingestion:** *Admin → Data health* → *Trigger a run* (CURRENT_PRICE). The run must end SUCCESS, or `scripts/health_check.sh` must pass after the next scheduled run.
   - **Email:** request a password reset for a mailbox you own at `https://agrinex.managio.me/forgot-password`. The email must arrive, and its link must start with `https://agrinex.managio.me/reset-password#token=`. Delivery is not proven until such an email has arrived.
6. **Revoke the previous key at the provider,** once the new one is confirmed:
   - Resend: delete the old key on https://resend.com/api-keys.
   - data.gov.in: the portal does not document whether generating a new key revokes the old one. From the development machine, send one request with the old key and confirm it is refused, using the government-key command of step 4 with the old key. If it still works, ask data.gov.in support to revoke it.
   - Then remove the old key from local files too (`infra/.env`), or replace it with the new one for local development.

## 10. SSH access and recovery

- **Normal access:** key-based SSH as `ubuntu` with the deployment key `~/.ssh/agrinex_oci` on the development machine. Ubuntu cloud images do not allow password logins over SSH.
- **Recommended hardening (owner):**
  - add a second, personal SSH public key to `/home/ubuntu/.ssh/authorized_keys`, so one lost laptop does not lock everyone out;
  - keep a copy of the deployment key in the owner's password manager;
  - optionally limit TCP 22 in the VCN security list to the owner's IP. With a changing home IP, this rule must then be updated in the console before connecting.
- **Recovery when SSH fails** (lost key, a firewall mistake, a stuck boot): the Oracle Console's **instance console connection**, the serial console.
  - Open it from the instance page → *Console connection* → **Launch Cloud Shell connection**.
  - It needs no open port and no existing key.
  - Logging in there needs a local password for `ubuntu`. Set one once, right after the first SSH login, with `sudo passwd ubuntu`, and keep it in the password manager. SSH still refuses passwords.
  - Oracle documents this for "previously working instances that stop responding", Ampere instances included.
- **Last resort:** the data is recoverable from the off-VM backup copies (section 7), on a new VM following section 4.

## 11. Residual risks accepted for the first deployment

| Risk | Why it remains | Mitigation |
|---|---|---|
| The data.gov.in key was exposed before deployment | The owner deferred its rotation (2026-10-18). Anyone who saw it can use the owner's quota and identity at data.gov.in. It does not give access to AgriNex or its data | Rotate it first thing after deployment (section 9). The key never reaches the browser, the repository or the images |
| The Resend key has full account access | Its replacement was deferred with the government key | Replace it with a sending-only key restricted to `managio.me` (section 9) |
| The production ADMIN password is the local one, also kept in plain text in local `infra/.env` | On 2026-10-18 the stored ADMIN password in the local database, and therefore in the curated dump, still matched `infra/.env` | Change it right after the first production sign-in (section 4.6). The server's bootstrap variables stay empty |
| No availability guarantee; the idle-reclamation policy (section 2) | Always Free tier | Off-VM backup copies (section 7); the rebuild path of section 4 |

## 12. Deployment record

| Date | Step | Result |
|---|---|---|
| 2026-10-18 | Images built and the curated copy restored on a local rehearsal stack (`infra/docker-compose.dev.yml`) | The stack becomes healthy. V13 seeds the ledger: 362 settled dates, 1 unsettled, 3 pending. The edge routes: `/` 200, `/api/health` 200, public API 200, `/actuator/health` 404, admin API without a token 401. A real comparison through the edge ranks 38 markets |
| 2026-10-18 | Local verification of the release (guide §10) | Backend 112 of 112, frontend 196 of 196, type-check and lint pass, browser journeys 10 of 10. V13 applied to the development database (backup `local-reference-data/backups/agrinex_pre_V13_2026-10-18.dump`). A real HISTORICAL run for 2026-10-15 to 2026-10-17 stored 2,930 new observations (completeness 98.14 %); 2026-10-15 grew from 1,192 records at age 1 to 1,639 at age 3, and became SETTLED |
| 2026-10-18 | Pre-deployment security fixes (profiles, startup checks, admin authorization, Referrer-Policy, source bundle) | Backend 157 of 157, frontend 199 of 199, type-check, lint and build pass, browser journeys 10 of 10. The production Nginx template passes `nginx -t`. With a throwaway certificate on this machine, it redirects HTTP to `https://agrinex.managio.me`, serves the ACME path, and sends HSTS, `nosniff`, `DENY` and one Referrer-Policy per response (`no-referrer` on the emailed-link pages). It answers 404 for `/actuator/` and 401 for the admin API. The images build from the source bundle alone (617 files, manifest verified) |
| 2026-10-18 | Final pre-deployment fixes: HSTS one day, fixed internal addresses, `set_env_value.sh`, guide sections 5 and 9–11 | Production template: `nginx -t` passes. Over HTTPS with a throwaway certificate it sends `max-age=86400`, and exactly one Referrer-Policy per response. After `docker compose up -d --no-deps --force-recreate` of the backend, then of the frontend, the edge kept answering 200 (backend .20, frontend .21). `set_env_value.sh` and `create_server_env.sh` were tested on synthetic files |
| — | Oracle VM, DNS, certificate, production start | Waiting for the owner actions of section 3 |

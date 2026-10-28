---
name: agrinex-cloud
description: AgriNex deployment and operations within the approved topology. Covers one cloud VM running Docker Compose with Nginx, certbot, the backend, the frontend, PostgreSQL, Redis and the optional ML batch job, plus environment configuration and secrets, TLS and security headers, health checks, backups, the observability limits, operator procedures on the host, and public-deployment gates. Use this skill whenever a task touches Dockerfiles, compose files, Nginx configuration, environment variables, secrets, TLS certificates, hosting, CI/CD, health checks, backups, logs, or the staging or public deployment, even if framed as "just run it on a server".
---

# AgriNex cloud and operations

## Purpose

Deploy and operate AgriNex exactly as the approved topology defines it: one VM, Docker Compose, one HTTPS origin, and secrets only in the deployment environment. Add no infrastructure the SSOT does not name.

This skill points to the authoritative documents. If it disagrees with them or with a binding decision, they win: correct this file.

## When to use

- Writing or reviewing Dockerfiles, the compose files, Nginx configuration or `infra/.env.example`.
- Planning the staging or public deployment, TLS, backups, health checks or logs.
- Choosing CI/CD at scaffolding.
- Running operator procedures on the host.

## Source hierarchy

This follows CLAUDE.md §B:

1. The master specification: §7, §10, §28, §37, §42 (items 27–28), §43, §45.
2. Binding decision-log entries.
3. `docs/architecture/DEPLOYMENT_ARCHITECTURE.md`, `CONTAINER_ARCHITECTURE.md`, `SECURITY_ARCHITECTURE.md`, `RESILIENCE_ARCHITECTURE.md`, and `CURRENT_TECHNOLOGY_BASELINE.md`.
4. Current official Docker, Compose, Nginx, certbot, PostgreSQL and Redis documentation for the baseline versions, fetched with Context7.

## Required reading (READ SSOT FIRST)

1. `CLAUDE.md` §F, §J and §K, then `docs/00_SSOT_INDEX.md` area 7, and `docs/REQUIRES_DECISION.md`. RD-037 (hosting) and RD-002 (`.mcp.json` credentials) are open at the time of writing; check the register.
2. `docs/architecture/DEPLOYMENT_ARCHITECTURE.md`: deployment view, services, environment configuration, startup, environments, TLS, health, operational requirements, CI/CD.
3. `docs/architecture/CURRENT_TECHNOLOGY_BASELINE.md`: Docker Engine 29.x, Compose v5.x, and the pinned images.
4. Decision-log entries:
   - ADR-0017 (routing, CORS, browser tokens, rate limits, headers);
   - ADR-0018 (Redis), ADR-0020 (admin boundary and secrets), ADR-0021 (topology);
   - ADR-0028 (credentials and demo data), ADR-0030 (optional ML job), ADR-0033 (MCP credentials);
   - ADR-0037 and ADR-0041 (operator procedures), ADR-0042 (security outcomes).

## Non-negotiable AgriNex rules

- **Topology** (ADR-0021): one cloud VM running the root `docker-compose.yml`:
  - `agrinex-nginx`, `agrinex-frontend`, `agrinex-backend`, `agrinex-db`, `agrinex-redis`;
  - optionally `agrinex-ml`, a batch job with no port (ADR-0030);
  - certbot on the host.

  Kubernetes, microservices, managed brokers, WAFs, identity providers, secrets managers, metrics stacks and log aggregators are not part of the architecture. Adding one needs a decision.
- **Edge** (ADR-0017):
  - one public HTTPS origin, with only ports 80 and 443 published;
  - `/api/v1/` routes to the backend, and every other path to the frontend;
  - `/actuator/` answers 404 at Nginx;
  - plain HTTP redirects to HTTPS, except the ACME path;
  - the security headers of ADR-0017 item 7, with HSTS once TLS is verified;
  - the final CSP is a frontend release gate (ADR-0042, SR-11).
- **Secrets** (ADR-0020 items 3–5; CLAUDE.md §K):
  - they live only in the root `.env` file on the server (mode 600, never committed);
  - `infra/.env.example` lists names only;
  - never put a secret in images, logs, documentation, chat or `NEXT_PUBLIC_*` variables;
  - do not commit `.mcp.json` until RD-002's user actions are done (ADR-0033).
- **Credentials.** No fixed or published credentials. The bootstrap admin password is removed from the environment after first start (ADR-0028). Demo fixtures load only in demo-profile environments, never in the public deployment (departure D-12).
- **Redis** is password-protected, without persistence, and holds only rate-limit counters and the reference cache (ADR-0018).
- **Rate limits.** The values are deployment configuration (ADR-0017). Add no general per-IP limit; tuning follows real traffic (ADR-0042, SR-13).
- **Data source.** data.gov.in stays the only price source. During outages the system serves stored data (ADR-0005), and no operational workaround may substitute another source or synthetic data.
- **Health and observability:**
  - checks: actuator readiness inside the network, `pg_isready`, Redis `PING`, the frontend's `/api/health`, and `scripts/health_check.sh` (named by spec §43, not created yet);
  - logs are structured JSON with a correlation id, and never contain bodies, personal data or secrets (ADR-0042);
  - log aggregation, metrics and alerting backends are not specified and not added.
- **Backups.** A daily host `pg_dump`, keeping 7 days. Data survives restarts on named volumes (`DEPLOYMENT_ARCHITECTURE.md` §4.8).
- **Operator procedures.** Account erasure, deactivation and reactivation run as one-off backend commands on the host, by an operator with server access, following the runbook (ADR-0037, ADR-0041).
- **Hosting.** The provider, region, VM size and domain are a human decision (RD-037). Do not choose them.
- **CI/CD.** The CI system is chosen at infrastructure scaffolding (ADR-0034). The branches are `main`, `develop` and `feature/*` (§42).

## Common rules (apply to every AgriNex skill)

- Read the SSOT first.
- Do not invent:
  - requirements;
  - database fields;
  - API contracts;
  - external data.
- Do not bypass module ownership or silently override an ADR.
- Do not modify unrelated files.
- Security-sensitive changes need human review (CLAUDE.md rule 16).
- Every implementation task needs tests.
- Never mark work complete until its acceptance criteria pass.

## Allowed responsibilities

- Infrastructure files and deployment documentation within the approved topology, once a task authorizes scaffolding.
- Health checks, backups, and the operational runbook.
- Verifying TLS and headers.

## Forbidden responsibilities

- New infrastructure components, providers or managed services without a decision.
- Publishing internal ports.
- Committing or printing secrets.
- Changing CI, deployment or MCP configuration without explicit user instruction (CLAUDE.md §F).
- Deciding RD-037.

## Dependencies on other skills

- agrinex-architecture: topology and containers.
- agrinex-backend and agrinex-frontend: images and configuration.
- agrinex-qa: staging tests and Experiment 2.
- agrinex-release: deployment gates.

## Validation expectations

- `docker compose up -d` starts every service from `.env.example`.
- Migrations run on startup.
- Health endpoints respond (spec §10).
- Only ports 80 and 443 are reachable publicly, and `/actuator/` answers 404.
- TLS and headers are verified.
- No secret appears in images, logs or the repository.
- A backup restore has been tried before the public deployment. This is a verification practice, not an SSOT requirement.

## Escalation rules

- **Exposed credential found:** stop and tell the user. Do not rotate it or rewrite history (CLAUDE.md §K).
- **Hosting choices:** a human decision (RD-037).
- **Edge, TLS, CORS, rate-limit or secret changes:** security review.

## Output expectations

Report:
- the files changed;
- the services, ports and environment variable names (never values);
- the verification steps run with their results;
- the decisions cited;
- open items, for example RD-037.

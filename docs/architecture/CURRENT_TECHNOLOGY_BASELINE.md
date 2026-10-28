# AgriNex — Current Technology Baseline

## 1. Purpose

**Status: APPROVED — AUTHORITATIVE.** The project owner approved this baseline on 2026-10-03, recorded as ADR-0002 in `docs/architecture/DECISION_LOG.md` (CLAUDE.md §J and rule 14).

- The two open sub-decisions were settled the same day under the delegation recorded in ADR-0001: the build tool is Maven 3.10.x (ADR-0003), and the browser floor is accepted (ADR-0004).
- The version departures from the specification are listed in the decision log (D-01 to D-05).

**Verification date:** 2026-10-02. Every version, date, and support status below was checked against official sources on that date.

- **Re-verified 2026-10-03** against Maven Central, npm, nodejs.org and Docker Hub, with no change except `eclipse-temurin` 25.0.4.1_1, a patch release within the Java 25 line that the patch rule in §8 allows.
- The re-checked versions: Spring Boot 4.1.1, Next.js 16.3.8, React 19.3.0, Node.js 24.21.0, Maven 3.10.0, and the images `postgres:16.15`, `redis:7.4.11-alpine` and `nginx:1.30.5`.

This document identifies the **current, supported, and mutually compatible versions** for the technology families that the master specification (`docs/01_AGRINEX_MASTER_PRODUCT_PHASES.md`) establishes. It answered the open part of RD-001: which exact versions to scaffold with. The project owner approved it on 2026-10-03 (ADR-0002).

**Relationship to the master specification:**

- The specification names the technology families: header line 12, plus §12, §13, §28, §35, §44, and §45. **This document does not re-decide them.**
- The versions the specification states are treated as the **documented, historical baseline**: Java 17, Spring Boot 3.3.x, Next.js 14, PostgreSQL 16, Redis 7, Python 3.11, and `nginx:1.25`.
- The specification is not edited. Each difference between the documented and recommended versions is explained in §5. A difference takes effect only after approval. All of them took effect on 2026-10-03 and are recorded as departures D-01 to D-05 in the decision log.

This task was research and documentation only. Nothing was installed, scaffolded, or configured.

## 2. Baseline Rules

1. **Technology families come from the master specification.** The families are Java, Spring Boot, Next.js, TypeScript, PostgreSQL, Redis, Python (optional ML track), and Docker. The specification also names these technologies explicitly:
   - Tailwind CSS (§12) and Recharts (§13);
   - Flyway (§12);
   - Spring Security, JPA/Hibernate, Bean Validation, and BCrypt (§28);
   - Nginx (§12, §45) and certbot (§28);
   - MSW (§44) and k6 (§48);
   - FastAPI, Prophet, and LightGBM (§12, §35; optional).

   None of these choices is reopened here.
2. **Versions require current verification.** The specification's versions are a historical baseline and are not treated as current by default.
3. **Newest is not automatically preferred.** Each version is judged on:
   - its official support window across the project horizon;
   - its LTS or stable status;
   - production stability and ecosystem maturity;
   - compatibility with the rest of the stack;
   - whether official migration documentation exists.
4. **Minimal change.** A version the specification states is kept if it is officially supported across the project horizon and compatible with the rest of the stack. On that basis, PostgreSQL 16 and Redis 7 are kept.
5. **Compatibility is evaluated as a complete stack** (§4). Versions managed by Spring Boot's dependency management are taken as one tested set and should not be overridden without a decision.
6. **Official documentation is the primary source.** Sources used:
   - vendor documentation and release notes;
   - support and EOL policies;
   - official registries: Maven Central, npm, PyPI, Docker Hub, and the GitHub releases API.

   Context7 was used once, to read Lettuce's official compatibility roadmap. Blogs and forums were not used. Where an official source could not be retrieved, this document says so.
7. **Nothing is silently added.** For a technology the specification does not name, the outcome depends on whether a neutral default exists:
   - If one exists, such as npm bundled with Node.js, it is marked "not named in the SSOT" and recommended openly.
   - If none exists, such as the choice between Maven and Gradle, it is recorded as REQUIRES_DECISION.

**Decision-status labels:**

| Label | Meaning in this document |
|---|---|
| VERIFIED | Authoritative evidence establishes the fact. Examples: the specification's version is still supported, or a version is fixed by Spring Boot's managed dependency set |
| RECOMMENDED | The evidence supports this version as the project choice. Since ADR-0002 (2026-10-03), every RECOMMENDED row is part of the approved baseline |
| REQUIRES_DECISION | A genuine choice remained (§7). Both such choices are now resolved: ADR-0003 and ADR-0004 |
| DECIDED | Settled under the delegation recorded in ADR-0001 |
| COMPATIBILITY_CONFLICT | Two required components have no compatible supported combination |

**Project horizon (ASSUMPTION).** The specification gives a 12-week development order (§42) but no calendar dates. This baseline assumes implementation starts in October 2026. It requires official support to continue at least through the end of 2027, covering development, evaluation, the demo, and a buffer. Every recommended version is supported beyond that point.

### Not established by the SSOT: status after the decision lock

The specification does not name the items below. CLAUDE.md rule 15 requires a justified decision for each.

| Item | Status |
|---|---|
| JWT library | Decided (ADR-0016): Spring Security's OAuth2 resource-server JWT support (Nimbus, Boot-managed) for HS256. No additional library |
| HTTP client for the government API | Decided (ADR-0011): Spring Framework's `RestClient` (spring-web, Boot-managed). No additional library |
| Transactional email (account emails) | Decided (ADR-0048): Resend's HTTPS API (`POST /emails`, Bearer key), called with the same `RestClient`. No email SDK and no additional library. Resend is an external service, not a versioned dependency; its API is verified by a live test at implementation (spec §39) |
| OpenAPI specification version | Decided (ADR-0022): OpenAPI 3.1, hand-authored `contracts/openapi.yaml` (departure D-11) |
| Rate limiter | Decided (ADR-0018): fixed-window counters with Spring Data Redis. No additional library |
| i18n library | Deferred to frontend scaffolding (ADR-0034) |
| Frontend unit-test and E2E test tools | Deferred to frontend scaffolding (ADR-0034) |
| Contract-validation library; optional OpenAPI generator for drift reports | Deferred to scaffolding (ADR-0034) |
| CI system | Deferred to infrastructure scaffolding (ADR-0034) |
| ESLint configuration | Deferred to frontend scaffolding (ADR-0034). It is a Next.js default, not named in the specification; see its TypeScript constraint in §4 |

Spring Batch appears only conditionally (§43, "if not in Spring Batch"). The specification's ingestion design uses the Spring Scheduler instead (§12, §15, §22), so Spring Batch is not included.

## 3. Technology Matrix

All URLs were verified on 2026-10-02. "Boot-managed" means Spring Boot 4.1.1's dependency management, read from `spring-boot-dependencies-4.1.1.pom` on Maven Central, fixes the version.

### Backend (Java / Spring)

| Technology | SSOT Version | Current Supported Version | Recommended Version | Support Status | Compatibility | Decision Status | Official Source |
|---|---|---|---|---|---|---|---|
| Java (JDK) | 17 | LTS releases: 17, 21, 25 (latest LTS; Temurin 25.0.4). Non-LTS: 27 (GA 2026-09-15). 26 reached end of support in Sep 2026 | **Java 25 (LTS)** | Eclipse Temurin support: 25 until at least Sep 2031; 21 until at least Dec 2029; 17 until at least Oct 2027 | Boot 4.1.1 supports Java 17–26. Spring Framework 7 recommends "JDK 25 or higher for production" | RECOMMENDED | https://adoptium.net/support/ · https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-Versions · https://openjdk.org/projects/jdk/ |
| Spring Boot | 3.3.x | 4.1.x: 4.1.1 is current, with OSS support to 2027-07-31. 4.0.x: 4.0.8, OSS support to 2026-12-31. 4.2.x: milestones only, initial release planned for 2026-11-30 | **4.1.x** (4.1.1) | **3.3.x OSS support ended 2025-06-30.** 3.5.x OSS support ended 2026-06-30 | Requires Java 17+, compatible up to Java 26. Requires Spring Framework 7.0.9+. Maven 3.6.3+ or Gradle 8.14+ / 9.x | RECOMMENDED | https://api.spring.io/projects/spring-boot/generations · https://docs.spring.io/spring-boot/system-requirements.html |
| Spring Framework | Not stated (Boot-managed) | 7.0.x is "the current production line", with OSS support to 2027-07-31. 7.1.x is due in Nov 2026. 6.2.x OSS support ended 2026-06 | **7.0.x** (7.0.9, Boot-managed) | Supported | JDK 17–25+. Jakarta EE 11 (Servlet 6.1, JPA 3.2, Bean Validation 3.1) | VERIFIED (Boot-managed) | https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-Versions · https://api.spring.io/projects/spring-framework/generations |
| Spring Security | Not versioned (§28) | 7.1.x, with OSS support to 2027-07-31 | **7.1.x** (7.1.1, Boot-managed) | Supported | Provides the BCrypt and `@PreAuthorize` method security that §28 requires | VERIFIED (Boot-managed) | https://api.spring.io/projects/spring-security/generations |
| Spring Data (JPA, Redis) | Not versioned | 2026.0.x, with OSS support to 2027-07-31 | **2026.0.x** (2026.0.1, Boot-managed) | Supported | Aligned with Boot 4.1.x | VERIFIED (Boot-managed) | https://api.spring.io/projects/spring-data/generations |
| Hibernate ORM / Hibernate Validator (JPA, Bean Validation, §28) | Not versioned | ORM 7.4; Validator 9.1 | **ORM 7.4.5.Final; Validator 9.1.3.Final** (Boot-managed) | Current | Jakarta Persistence 3.2 and Validation 3.1. The PostgreSQL dialect requires PostgreSQL 14.0 or later | VERIFIED (Boot-managed) | https://docs.hibernate.org/orm/7.4/dialect/dialect.html |
| Servlet container | Not stated (Boot default) | Tomcat 11.0.x or Jetty 12.1.x (Servlet 6.1). Boot 4 dropped Undertow | **Tomcat 11.0.x** (11.0.24, Boot-managed) | Current | Servlet 6.1, as Boot 4 requires | VERIFIED (Boot-managed) | https://docs.spring.io/spring-boot/system-requirements.html |
| Flyway (§12) | Not versioned | 12.4.0 | **12.4.0** (Boot-managed), plus `flyway-database-postgresql` and `spring-boot-starter-flyway` | Current | PostgreSQL 12–18 listed as supported | VERIFIED | https://documentation.red-gate.com/flyway/getting-started-with-flyway/system-requirements/supported-databases-and-versions |
| PostgreSQL JDBC driver (pgJDBC) | Not stated | 42.7.13, the "current release" | **42.7.13** (Boot-managed) | Current | Driver for PostgreSQL 16 | VERIFIED | https://jdbc.postgresql.org/ |
| Redis client | Not stated | Lettuce 7.5.2.RELEASE is Boot's default. Jedis 7.4.1 is also managed | **Lettuce** (via `spring-boot-starter-data-redis`) | Current | Lettuce 7.x is tested against Redis 2.6–8.4 | VERIFIED | https://docs.spring.io/spring-boot/reference/data/nosql.html · https://github.com/redis/lettuce/blob/main/docs/compatibility-roadmap.md |
| Backend test stack (not named in the SSOT; Boot-managed if `spring-boot-starter-test` is used) | Not named | JUnit Jupiter 6.0.3, Mockito 5.23.0, AssertJ 3.27.7 | **As managed by Boot 4.1.1** | Current | Boot 4 removed `@MockBean`; use `@MockitoBean` instead | VERIFIED (Boot-managed) | https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide |
| Build tool | **Not established** in the SSOT | Maven 3.10.0 ("recommended version for all users"; Maven 4 is RC only). Gradle 9.8.0 | **Maven 3.10.x**, pinned with the Maven Wrapper | Maven 4.x: "NOT safe for production use" | Boot 4.1.1 supports Maven 3.6.3+ and Gradle 8.14+ or 9.x. Java 25 needs Gradle 9.1.0+ | **DECIDED (ADR-0003)** | https://maven.apache.org/download.cgi · https://docs.gradle.org/current/userguide/compatibility.html |

### Frontend

| Technology | SSOT Version | Current Supported Version | Recommended Version | Support Status | Compatibility | Decision Status | Official Source |
|---|---|---|---|---|---|---|---|
| Next.js | 14 | 16.x is Active LTS (16.3.8, published 2026-09-30). 15.x is Maintenance LTS until about 2026-10-21. **14.x is unsupported** | **16.3.x** | Active LTS | Requires Node ≥20.9.0 and React ^18.2.0 or ^19.0.0. Minimum TypeScript 5.1.0. Supports Chrome/Edge/Firefox 111+ and Safari 16.4+ | RECOMMENDED | https://nextjs.org/support-policy · https://nextjs.org/docs/app/getting-started/installation |
| React | Not stated | 19.3.0 (2026-09-09). The 19.2.x, 19.1.x, and 19.0.x lines still receive patches | **19.3.x** (react and react-dom) | Current | Next 16 accepts React ^19, and the App Router uses React builds bundled with Next.js | RECOMMENDED | https://react.dev/versions |
| TypeScript | Not versioned | 7.0.2 (native compiler, released 2026-07-08, no JavaScript compiler API). 6.0.3 (2026-04-16) | **6.0.x** (6.0.3) | No formal support policy is published | Next ≥5.1. typescript-eslint supports only `>=4.8.4 <6.1.0`. MSW 3 requires ≥5.9 | RECOMMENDED | https://devblogs.microsoft.com/typescript/announcing-typescript-7-0/ · https://typescript-eslint.io/users/dependency-versions |
| Node.js | Not stated | 24 "Krypton", Active LTS (24.21.0). 22, Maintenance LTS. 26, Current (becomes LTS 2026-10-28). **20 reached end of life 2026-04-30** | **24 LTS** (24.21.0) | Active LTS until 2026-10-20, then Maintenance LTS until EOL on 2028-04-30 | Next 16 requires ≥20.9. MSW, typescript-eslint, and Recharts all support it | RECOMMENDED | https://nodejs.org/en/about/previous-releases · https://github.com/nodejs/Release |
| Package manager (not named in the SSOT) | Not established | npm 11.19.0, bundled with Node 24.21.0 | **npm** (bundled with Node.js) | Ships with Node | Next.js documents npm, pnpm, yarn, and bun | RECOMMENDED (neutral default) | https://nodejs.org/dist/index.json |
| Tailwind CSS (§12) | Not versioned | 4.3.3 (2026-07-16). 3.4.19 (`v3-lts` tag, last published 2025-12-10) | **4.3.x** | Current | Requires Chrome 111+, Safari 16.4+, Firefox 128+. Not designed for use with Sass, Less, or Stylus | RECOMMENDED; browser floor accepted (ADR-0004) | https://tailwindcss.com/docs/compatibility |
| Recharts (§13) | Not versioned | 3.10.1 (2026-07-25) | **3.10.x** | Current | Peer dependency on React ^19 is supported. Node ≥18 | RECOMMENDED | https://github.com/recharts/recharts/releases |
| MSW (§44) | Not versioned | 3.0.1 (3.0.0 released 2026-09-28: ESM-only, Node ≥22, TypeScript ≥5.9). 2.15.0 (2026-07-08) | **2.15.x** | 2.x maintenance after 3.0 is not documented | Requires Node ≥18 and TypeScript ≥4.8. The Next.js App Router support issue (#1644) is closed as "completed" | RECOMMENDED | https://mswjs.io/ · https://github.com/mswjs/msw/releases/tag/v3.0.0 |

### Data

| Technology | SSOT Version | Current Supported Version | Recommended Version | Support Status | Compatibility | Decision Status | Official Source |
|---|---|---|---|---|---|---|---|
| PostgreSQL | 16 | 18.6, 17.11, 16.15, 15.19, and 14.24 are supported. 19 is in beta | **16** (current minor 16.15) | **Supported until 2028-11-09** | Hibernate requires ≥14. Flyway supports 12–18. pgJDBC is at its current release. The official `postgres:16.15` image exists | VERIFIED (SSOT version retained) | https://www.postgresql.org/support/versioning/ |
| Redis | 7 (`redis:7-alpine`) | Extended releases: 7.2 and 7.4 (EOL 2029-12-01), and 8.2 (EOL 2030-09-01). Standard releases: 8.4–8.10. 8.0 reaches EOL 2026-12-01 | **7.4** (7.4.11) | Extended release; EOL 2029-12-01 | Lettuce 7.x is tested against 7.2 and 7.4. The official `redis:7.4.11-alpine` image exists | RECOMMENDED (the SSOT major version is VERIFIED as supported) | https://redis.io/docs/latest/operate/oss_and_stack/install/version-mgmt/ · https://redis.io/legal/licenses/ |

### Optional ML track (non-blocking)

| Technology | SSOT Version | Current Supported Version | Recommended Version | Support Status | Compatibility | Decision Status | Official Source |
|---|---|---|---|---|---|---|---|
| Python | 3.11 | 3.14 is in "bugfix" status (3.14.8). 3.13, 3.12, and 3.11 receive security fixes only. 3.10 reached end of life 2026-10-01. 3.15 is a prerelease | **3.14** (3.14.8) | Bugfix status; end of life 2030-10 | NumPy 2.5.3 requires Python ≥3.12. FastAPI, Prophet, LightGBM, pandas, and statsmodels all declare 3.14 support | RECOMMENDED (optional, non-blocking) | https://devguide.python.org/versions/ |
| FastAPI | Not versioned | 0.142.2 (2026-09-30) | **0.142.x** | Uses 0.x versioning, so pin it exactly | Requires Python ≥3.10. Classifiers declare support through 3.14 | RECOMMENDED (optional) | https://pypi.org/project/fastapi/ |
| Prophet, LightGBM (§35) | Not versioned | prophet 1.4.0; lightgbm 4.7.0 | Pin only if the ML track starts (HARD RULE 2) | Current | Both declare Python 3.10–3.14 support | RECOMMENDED (deferred, optional) | https://pypi.org/project/prophet/ · https://pypi.org/project/lightgbm/ |

### Infrastructure and tooling

| Technology | SSOT Version | Current Supported Version | Recommended Version | Support Status | Compatibility | Decision Status | Official Source |
|---|---|---|---|---|---|---|---|
| Docker Engine | Not versioned | 29.8.2 (2026-09-30) | **29.x** | No published support window. "Docker doesn't provide support for Docker Engine" (Moby project) | Official images exist for every recommended runtime (§8) | RECOMMENDED | https://docs.docker.com/engine/release-notes/ · https://docs.docker.com/engine/install/ |
| Docker Compose | Not versioned (the SSOT uses the `docker compose` CLI) | v5.6.0 (2026-10-02) | **v5.x** | Current | The top-level `version` field is obsolete and produces a warning | RECOMMENDED | https://github.com/docker/compose/releases · https://docs.docker.com/reference/compose-file/version-and-name/ |
| Nginx | 1.25 (`nginx:1.25`) | Stable 1.30.5; mainline 1.31.6. **1.25 is not listed, not even as a legacy version** | **1.30.x stable** (1.30.5) | Current stable | Works with the stack through standard reverse proxy and TLS termination (§12) | RECOMMENDED | https://nginx.org/en/download.html |
| JDK distribution for images (not named in the SSOT) | Not established | Eclipse Temurin 25.0.4 (official `eclipse-temurin` image) | **Temurin 25** (JDK image for builds, JRE image for runtime) | Supported until at least Sep 2031 | — | RECOMMENDED (neutral default) | https://adoptium.net/support/ |
| k6 (§48) | Not versioned | v2.3.0 (2026-09-21) | Current v2.x when QA starts | Current | Standalone CLI | RECOMMENDED (tooling, non-blocking) | https://github.com/grafana/k6/releases |
| certbot (§28) | Not versioned | v5.8.0 (2026-09-01) | Current maintained release at deployment | Current | Runs on the host or VM | RECOMMENDED (tooling, non-blocking) | https://github.com/certbot/certbot/releases |

## 4. Compatibility Matrix

| Relationship | Versions | Result | Evidence |
|---|---|---|---|
| Java ↔ Spring Boot | Java 25 ↔ Boot 4.1.1 | **Compatible** | "requires at least Java 17 and is compatible with versions up to and including Java 26" (Boot system requirements) |
| Java ↔ Spring Framework | Java 25 ↔ Framework 7.0.x | **Compatible** | "We fully test and support Spring on … JDK 17, JDK 21, as well as JDK 25 … We recommend JDK 25 or higher for production use with Spring Framework 7.x" |
| Java 27 ↔ Spring Boot 4.1.1 | — | **Uncertain** (do not use) | start.spring.io offers Java 27, but Boot 4.1.1's documented range ends at Java 26 |
| Spring Boot ↔ Spring Framework / Security / Data | 4.1.1 ↔ 7.0.9 / 7.1.1 / 2026.0.1 | **Compatible** | Managed together in `spring-boot-dependencies-4.1.1`. All share OSS support until 2027-07-31 |
| Spring Boot ↔ Maven / Gradle | Boot 4.1.1 ↔ Maven ≥3.6.3 or Gradle 8.14+/9.x | **Compatible** (Maven chosen: ADR-0003) | Boot system requirements |
| Java 25 ↔ Gradle / Maven | Gradle ≥9.1.0; Maven 3.10.0 | **Compatible with constraints** (Gradle must be 9.1.0 or later) | Gradle compatibility matrix; Maven 3.10+ runs on JDK 8+ |
| Spring Boot ↔ Servlet container | Boot 4.1.1 ↔ Tomcat 11.0.24 (Servlet 6.1) | **Compatible** | Boot system requirements. Undertow is no longer supported |
| Spring Boot ↔ PostgreSQL JDBC driver | Boot 4.1.1 ↔ pgJDBC 42.7.13 | **Compatible** | Boot-managed version; pgJDBC's current release |
| JPA / Flyway / JDBC ↔ PostgreSQL | Hibernate 7.4, Flyway 12.4.0, pgJDBC 42.7.13 ↔ PostgreSQL 16 | **Compatible** | Hibernate's PostgreSQL dialect requires ≥14.0. Flyway lists PostgreSQL 12–18 as supported |
| Flyway ↔ Spring Boot 4 | — | **Compatible with constraints** | Boot 4 needs `spring-boot-starter-flyway`, and Flyway needs the separate `flyway-database-postgresql` module (Boot 4.0 Migration Guide; Flyway documentation) |
| Spring Boot ↔ Redis client ↔ Redis | Lettuce 7.5.2 ↔ Redis 7.4 | **Compatible** | Lettuce is Boot's default client. Lettuce 7.x is tested against Redis 2.6–8.4, and its 7.7.0 release notes list 7.2 and 7.4 explicitly |
| Spring Boot ↔ test stack | Boot 4.1.1 ↔ JUnit Jupiter 6.0.3 / Mockito 5.23.0 | **Compatible** | Boot-managed. `@MockBean` and `@SpyBean` were removed in Boot 4 |
| Next.js ↔ React | Next 16.3.8 ↔ React 19.3 | **Compatible** | Peer dependency `react: ^18.2.0 \|\| … \|\| ^19.0.0`. The App Router uses React builds bundled with Next.js |
| Next.js ↔ Node.js | Next 16.3.8 ↔ Node 24 | **Compatible** | `engines.node >=20.9.0` |
| Next.js ↔ TypeScript | Next 16.3 ↔ TypeScript 6.0.3 | **Compatible** | Minimum TypeScript is 5.1.0 |
| Next.js ↔ TypeScript 7 | Next 16.3 ↔ TypeScript 7.0.2 | **Compatible with constraints** | Needs the experimental `useTypeScriptCli` path, because TypeScript 7.0 "does not currently provide the JavaScript compiler API" |
| ESLint (`eslint-config-next`) ↔ TypeScript | typescript-eslint ^8.46 ↔ TypeScript 6.0 / 7.0 | **Compatible with 6.0; incompatible with 7.0** | typescript-eslint supports TypeScript `>=4.8.4 <6.1.0` |
| Next.js ↔ Tailwind CSS ↔ browsers | Next 16 floor: Chrome/Edge/Firefox 111, Safari 16.4. Tailwind 4 floor: Chrome 111, Safari 16.4, Firefox 128 | **Compatible with constraints.** The floor is accepted (ADR-0004): Android 7.0 or later receives Chrome 119 or newer, which meets it; Android 6.0 and older are unsupported | The combined floor is Chrome/Edge 111+, Safari 16.4+, Firefox 128+. The specification's Persona 2 uses an "Older Android 4.7"" device (§6) but defines no minimum browser |
| Recharts ↔ React | 3.10.1 ↔ React 19 | **Compatible** | Peer dependency `react: … \|\| ^19.0.0` |
| MSW ↔ Node / TypeScript / Next.js | MSW 2.15 ↔ Node 24, TypeScript 6.0, Next 16 | **Compatible** | `engines.node >=18` and peer `typescript >= 4.8.x`. The App Router issue #1644 is closed as completed |
| MSW 3 ↔ Node 20 | — | **Incompatible** | MSW 3 requires Node ≥22. This matters only for the local workstation (§6) |
| Python ↔ FastAPI | Python 3.14 ↔ FastAPI 0.142.2 | **Compatible** | `requires_python >=3.10`. Classifiers declare support through 3.14 |
| Python ↔ ML ecosystem | Python 3.14 ↔ NumPy 2.5.3, pandas 3.0.6, statsmodels 0.15.0, Prophet 1.4.0, LightGBM 4.7.0 | **Compatible** | PyPI metadata and wheels |
| Python 3.11 ↔ current NumPy | Python 3.11 ↔ NumPy 2.5.3 | **Incompatible** (only older NumPy releases install) | NumPy 2.5.3 declares `requires_python >=3.12` |
| Docker ↔ selected runtime images | Engine 29.x / Compose v5.x ↔ the images in §8 | **Compatible** | All tags were checked on Docker Hub on 2026-10-02 |
| Compose ↔ the specification's commands | `docker compose up -d`, `docker compose exec` (§45) | **Compatible** | Compose v5 is the `docker compose` CLI. Omit the obsolete `version:` field |
| Frontend ↔ Backend API architecture | Next.js 16 ↔ Spring Boot 4.1 over REST/JSON `/api/v1/*`, JWT Bearer, through Nginx (§12) | **Compatible** (no version coupling). The contract tooling is **not established** | They interact only at the protocol level. `contracts/openapi.yaml` does not exist yet, and the OpenAPI version and generator are not established (§2) |

**COMPATIBILITY_CONFLICT:** none in the recommended baseline. The incompatibilities above affect only rejected alternatives (TypeScript 7 with typescript-eslint, Python 3.11 with current NumPy) or the current workstation (Node 20 with MSW 3).

## 5. Version Decisions

All of the following are **APPROVED** (ADR-0002, 2026-10-03). AgriNex has no application code yet. "Migration impact" therefore means changes to conventions, documentation, and team knowledge, not code rewrites.

### 5.1 Java: 17 → 25 (LTS)

- **Documented baseline:** Java 17 (spec header line 12, §12).
- **Recommended baseline:** Java 25 LTS, using Eclipse Temurin 25.0.x.
- **Reason:** Spring's own guidance recommends JDK 25 or higher for production with Spring Framework 7.x, which Boot 4.x requires. Java 25 also has the longest vendor support window. Java 17 is still technically supported, by Boot 4.1.1 and by Temurin until at least Oct 2027, but its runway barely covers the project horizon.
- **Evidence:**
  - Spring Framework Versions wiki: "We recommend JDK 25 or higher for production use with Spring Framework 7.x".
  - Temurin support: 25 until at least Sep 2031; 21 until at least Dec 2029; 17 until at least Oct 2027.
  - Boot 4.1.1 is compatible with Java 17–26.
  - Oracle's Java SE support roadmap page returned HTTP 403 and could not be checked.
- **Why not 21:** Java 21 is also fully supported (Temurin until at least Dec 2029) and would be a valid fallback. It was not chosen because Spring's explicit production recommendation is 25+.
- **Migration impact:** none for code. Tooling must support Java 25: Gradle 9.1.0+ if Gradle is chosen, and the `eclipse-temurin:25` images.
- **Dependencies affected:** build tool minimum (Maven 3.10.x, ADR-0003), Docker base image, CI runtime, and developer JDK installs.

### 5.2 Spring Boot: 3.3.x → 4.1.x (and Spring Framework 6.1 → 7.0)

- **Documented baseline:** Spring Boot 3.3.x (header line 12, §12, §43). That implies Spring Framework 6.1.x.
- **Recommended baseline:** Spring Boot 4.1.x (4.1.1), using its managed set:
  - Spring Framework 7.0.9, Spring Security 7.1.1, Spring Data 2026.0.1;
  - Hibernate 7.4.5, Flyway 12.4.0, pgJDBC 42.7.13, Lettuce 7.5.2;
  - Tomcat 11.0.24, JUnit Jupiter 6.0.3.
- **Reason:**
  - **3.3.x OSS support ended 2025-06-30**, and the whole 3.x line's OSS support ended with 3.5.x on 2026-06-30. No Spring Boot 3.x generation has OSS support today.
  - 4.0.x OSS support ends 2026-12-31, inside the project horizon.
  - 4.1.x has OSS support until 2027-07-31, and its Framework, Security, and Data generations share that date.
  - 4.2.x is not GA (milestone M2), with an initial release planned for 2026-11-30.
- **Evidence:** the `api.spring.io` generation dates, the `spring-boot-dependencies-4.1.1.pom` BOM, the Boot system requirements, and the Spring Boot 4.0 Migration Guide.
- **Breaking changes relative to the documented baseline (team-knowledge impact):**
  - Jakarta EE 11 and Servlet 6.1; Undertow removed.
  - **Jackson 3**: group ID `tools.jackson` and renamed types.
  - Modularized starters, so Flyway needs `spring-boot-starter-flyway`.
  - `@MockBean` and `@SpyBean` removed in favor of `@MockitoBean`.
  - `@SpringBootTest` no longer provides MockMvc, WebClient, or TestRestTemplate automatically.
  - Spring Security 7.
  - Much tutorial material still targets 3.x, so implementers must use current documentation (CLAUDE.md rules 5–6).
- **Dependencies affected:** every backend module, the test stack, the Flyway setup, and the Redis client configuration.

### 5.3 Next.js: 14 → 16.3.x (with React 19, Node.js 24, TypeScript 6.0)

- **Documented baseline:** Next.js 14 (header line 12, §12, §43). React, Node.js, and TypeScript versions are not stated.
- **Recommended baseline:** Next.js 16.3.x, React 19.3.x, Node.js 24 LTS, TypeScript 6.0.x, and npm bundled with Node.js.
- **Reason:**
  - **Next.js 14.x is unsupported** under the official support policy; its last published patch is 14.2.35, from 2025-12-11.
  - 15.x is in Maintenance LTS until two years after its 2024-10-21 release, which is about 2026-10-21: 19 days after this verification.
  - 16.x is the only Active LTS major and received a security release on 2026-09-30.
- **Why Node.js 24 (not 26):**
  - Node's official guidance: "Production applications should only use Active LTS or Maintenance LTS releases."
  - On 2026-10-02, Node 26 is still "Current" (LTS begins 2026-10-28). Node 24 is Active LTS, supported until 2028-04-30.
  - Node 20, the version on the current workstation, reached end of life on 2026-04-30.
- **Why TypeScript 6.0 (not 7.0):**
  - TypeScript 7.0 "does not ship with an API".
  - Next.js supports it only through an *experimental* CLI path.
  - typescript-eslint, which `eslint-config-next` uses, supports only TypeScript `<6.1.0`.
  - Microsoft encourages adopting 6.0 to ease the move to 7.
- **Breaking changes relative to Next.js 14 (from the official v15 and v16 upgrade guides):**
  - Async-only request APIs: `cookies()`, `headers()`, `params`, and `searchParams`.
  - `middleware` renamed to `proxy`.
  - Turbopack is the default bundler.
  - `next lint` removed, and `next build` no longer lints.
  - `next/image` default changes.
  - `revalidateTag` takes a second argument.
  - Runtime config and AMP removed.
  - Raised browser floor (Chrome/Edge/Firefox 111+, Safari 16.4+).
- **Dependencies affected:** frontend conventions and folder structure under `frontend/app/` (§43), MSW integration, Tailwind, Recharts, the frontend Docker image, and CI.

### 5.4 Python (optional ML track): 3.11 → 3.14

- **Documented baseline:** Python 3.11 (header line 12, §12).
- **Recommended baseline:** Python 3.14 (3.14.8), FastAPI 0.142.x, and Prophet and LightGBM pinned only if the ML track starts.
- **Reason:**
  - Python 3.11 receives security fixes only, and reaches end of life in 2027-10.
  - The current scientific stack is leaving 3.11 behind: **NumPy 2.5.3 requires Python ≥3.12**.
  - Python 3.14 is the newest branch in "bugfix" status (end of life 2030-10), and FastAPI, Prophet, LightGBM, pandas, and statsmodels all declare support for it.
  - Python 3.15 is only a prerelease and was not chosen.
- **Evidence:** the Python devguide versions page and PyPI metadata (requires_python, classifiers, and wheels) for each package.
- **Migration impact:** none, because no ML code exists. This track stays optional and **non-blocking** (P9, HARD RULE 2). It does not affect Phase 1 scaffolding.
- **Dependencies affected:** `ml/` (§43) and the `agrinex-ml` image (§45).

### 5.5 Nginx: 1.25 → 1.30.x stable

- **Documented baseline:** `nginx:1.25` (§45).
- **Recommended baseline:** `nginx:1.30.5`, the current stable line.
- **Reason:** nginx.org lists 1.31.6 as mainline and 1.30.5 as stable, and 1.25 appears nowhere on the download page, not even among legacy versions. The stable branch fits a production reverse proxy.
- **Migration impact:** none, because no `nginx.conf` exists yet. Use current nginx documentation when writing it.
- **Dependencies affected:** `infra/nginx.conf` and the `agrinex-nginx` service.

### 5.6 Refinements without a change of major version

- **Redis 7 → 7.4.11 (pinned minor).**
  - The specification's floating `redis:7-alpine` tag currently resolves to the 7.4 line. Redis 7.4 is an *Extended* release with end of life on 2029-12-01, so the specification's major version stays.
  - The minor version is pinned for reproducibility.
  - **Licensing differs by version.** Redis ≤7.2 is BSD-3-Clause; 7.4 is RSALv2/SSPLv1; 8.x adds an AGPLv3 option. See the §6 licensing risk.
- **PostgreSQL 16 → 16 (current minor 16.15). No change.**
  - It is supported until 2028-11-09 and compatible with Hibernate, Flyway, and pgJDBC.
  - PostgreSQL 17 and 18 are supported but not needed. Moving would contradict the minimal-change rule without any requirement driving it.
- **TypeScript, Tailwind CSS, Recharts, MSW, Flyway, Docker, and Compose** were unversioned in the specification. This baseline pins them; it does not change them.

## 6. Risks

### Upgrade risks

- **Knowledge drift.** Spring Boot 4, Spring Security 7, Jackson 3, and Next.js 16 differ substantially from the versions the specification was written against. Agents and contributors must use version-matched documentation (Context7, official guides), not memory.
- **Upcoming minor releases.** Spring Boot 4.2 and Spring Framework 7.1 are due 2026-11-30. 4.1.x stays supported until 2027-07-31, so upgrading is optional within the horizon.

### Compatibility risks

- **Browser floor.** The supported frontend stack requires Chrome/Edge 111+, Safari 16.4+, and Firefox 128+. The specification's Persona 2 uses an "Older Android 4.7"" device and no minimum browser is defined. Devices limited to Android 6.0 or older are unsupported. The floor is accepted (ADR-0004).
- **Experimental TypeScript path.** Next.js uses the project-local `tsc` CLI for type checking, controlled by `experimental.useTypeScriptCli`. That option is experimental "and its behavior may change".
- **No JavaScript compiler API in TypeScript 7.** Any tool that needs the TypeScript compiler API (typescript-eslint and similar) keeps the project on TypeScript 6.0.x until TypeScript 7.1's API ships.

### EOL risks

- **Next.js 15** leaves maintenance around 2026-10-21. Do not use it.
- **Node.js 24** moves to Maintenance LTS on 2026-10-20 but remains supported until 2028-04-30. Node 26 becomes LTS on 2026-10-28 and could be adopted later through a separate decision.
- **Redis 8.0** reaches end of life on 2026-12-01. Do not use it.
- **Python 3.10** reached end of life on 2026-10-01.
- **The documented baseline itself:** Spring Boot 3.3.x, Next.js 14, and `nginx:1.25` are already outside support.

### Dependency risks

- **MSW 3.0** was released 2026-09-28, four days before verification, and includes many breaking changes. The baseline uses MSW 2.15.x. The project should re-evaluate 3.x once it matures, because 2.x maintenance after 3.0 is not documented.
- **TypeScript publishes no formal support policy.** 6.0.x is a bridge line toward 7.x.
- **FastAPI uses 0.x versions**, which can break between minor releases. Pin it exactly.
- **Use Spring Boot's managed versions.** Do not override them without a decision. For example, the Hibernate 7.4 documentation reflects 7.4.11 while Boot manages 7.4.5.
- **Redis licensing.** Redis 7.4 is RSALv2/SSPLv1, 7.2 is BSD-3-Clause, and 8.x is RSALv2/SSPLv1/AGPLv3. The project owner should confirm the license of the chosen line is acceptable when approving this baseline. This document draws no legal conclusion.
- **Tailwind CSS v3.** Not needed, because the browser floor is accepted (ADR-0004). It would have been the fallback for a lower floor: v3.4.19, whose last release was 2025-12-10.

### Tooling risks

- **Spring Initializr defaults are not a recommendation.** start.spring.io currently defaults to Java 17 and also offers Java 27, which is outside Boot 4.1.1's documented range. Select Java 25 explicitly.
- **Maven 4 is release-candidate only** and "NOT safe for production use". Use Maven 3.10.x (ADR-0003).
- **The current workstation does not yet meet the baseline.** Read-only checks on 2026-10-02 found the following. Upgrading it is a separate, human-approved step:

  | Tool | Installed | Baseline needs |
  |---|---|---|
  | Java | Oracle JDK 21.0.8 | Java 25 |
  | Maven | 3.9.11 | Meets Boot's 3.6.3 minimum; current is 3.10.0 |
  | Gradle | Not installed | 9.1.0+ if Gradle is chosen |
  | Node.js | 20.20.2 (EOL) | Node 24 LTS |
  | npm | 10.8.2 | npm 11.19.0 (bundled with Node 24.21.0) |
  | Python | 3.13.5 | 3.14 for the optional ML track |
  | Docker | 29.2.0 | 29.x (met) |
  | Docker Compose | v5.0.2 | v5.x (met) |

### Container and runtime risks

- **Docker Engine has no published support window.** Keep it updated. Docker Desktop's commercial terms differ from Docker Engine's Apache-2.0 license; this academic project should confirm it meets Docker's terms.
- **Compose v5.6.0 was released on the verification date.** Its notes say existing containers "may be recreated the first time you run `compose up` after upgrading".
- **Floating image tags drift.** Tags such as `postgres:16` and `redis:7-alpine` move with new releases. Pin them to exact versions (§8) and update minor versions deliberately. PostgreSQL recommends always running the current minor release.
- **Image variant for ML.** Choose the Python variant when the Dockerfile is written. `-slim` (Debian) is the recommended starting point; verify that wheels for the ML libraries are available for whichever variant is chosen.

## 7. Decisions (formerly REQUIRES_DECISION)

These were the open decisions of the baseline task. All are resolved, and `docs/REQUIRES_DECISION.md` records them under Resolved:

- **RD-001:** resolved by ADR-0002. The project owner approved the baseline on 2026-10-03.
- **RD-013:** resolved by ADR-0003. Maven 3.10.x, with the Maven Wrapper.
- **RD-014:** resolved by ADR-0004. The floor is accepted, and Tailwind CSS 4.3.x stays.

The analysis below is kept as the record that led to those decisions.

### RD-013 — Backend build tool: Maven or Gradle

- **Why unresolved:** neither the master specification, the repository, nor any authoritative project document establishes a build tool. Evidence gathered:
  - The specification never mentions Maven or Gradle. Its `src/main/java` layout (§43) fits both.
  - The repository contains no build files.
  - Both tools are fully compatible with Boot 4.1.1 and Java 25 (§4).
  - Neither tool is a neutral default:
    - IntelliJ's generated `.idea/.gitignore` lists a Maven-related file, which is IDE boilerplate.
    - Maven 3.9.11 is installed on the workstation.
    - start.spring.io defaults to Gradle.

    None of these is a project decision.
- **Options:**
  - **Maven 3.10.x.** Boot requires 3.6.3+. Maven 4 is RC only.
  - **Gradle 9.x.** Boot supports 8.14+ or 9.x. Java 25 requires 9.1.0+, and the current release is 9.8.0.
- **Blocked (until 2026-10-03):** backend scaffolding, the backend Dockerfile, and CI. Resolved by ADR-0003: Maven 3.10.x.

### RD-014 — Minimum supported browsers for farmer-facing screens

- **Why unresolved:**
  - The only supported Next.js major (16) requires Chrome/Edge/Firefox 111+ and Safari 16.4+, and Tailwind CSS v4 adds Firefox 128+. Chrome 111 dates from March 2023.
  - The specification requires a mobile-first interface (F1.21) and describes Persona 2 using an "Older Android 4.7"" device with intermittent 3G (§6), but it defines no minimum OS or browser version.
  - Whether that floor is acceptable for the target farmers is a product decision.
- **Options:**
  - Accept the floor. This is the recommended option, keeping Tailwind 4.3.x.
  - Require older browsers. That needs Next.js browser targeting and polyfills, and probably a move to Tailwind v3.4.x, whose last release was 2025-12-10.
- **Blocked (until 2026-10-03):** confirming the Tailwind CSS major version at frontend scaffolding. Resolved by ADR-0004: the floor is accepted, and Tailwind CSS 4.3.x stays.

## 8. Final Recommended Baseline

**Status: APPROVED** (ADR-0002, 2026-10-03; verified 2026-10-02, re-verified 2026-10-03).

At scaffolding time, pin the exact versions below. If a newer *patch* has been released within the same line by then, use it.

| Area | Component | Recommended baseline |
|---|---|---|
| Backend runtime | Java | **Java 25 LTS**, Eclipse Temurin 25.0.x (25.0.4 at verification) |
| Backend framework | Spring Boot | **4.1.x** (4.1.1). Use Boot's dependency management for every component below |
| | Spring Framework | 7.0.9 (Boot-managed) |
| | Spring Security | 7.1.1 (Boot-managed) |
| | Spring Data | 2026.0.1 (Boot-managed) |
| | Hibernate ORM / Validator | 7.4.5.Final / 9.1.3.Final (Boot-managed) |
| | Servlet container | Tomcat 11.0.24, Servlet 6.1 (Boot-managed) |
| | Flyway | 12.4.0, with `spring-boot-starter-flyway` and `flyway-database-postgresql` (Boot-managed) |
| | PostgreSQL JDBC | 42.7.13 (Boot-managed) |
| | Redis client | Lettuce 7.5.2.RELEASE through `spring-boot-starter-data-redis` (Boot-managed) |
| | Test stack, if the Boot test starter is used | JUnit Jupiter 6.0.3, Mockito 5.23.0, AssertJ 3.27.7 (Boot-managed) |
| | Build tool | **Maven 3.10.x** with the Maven Wrapper (ADR-0003) |
| Frontend | Next.js | **16.3.x** (16.3.8) |
| | React / React DOM | **19.3.x** (19.3.0) |
| | TypeScript | **6.0.x** (6.0.3) |
| | Node.js | **24 LTS** (24.21.0) |
| | Package manager | npm, bundled with Node 24 (11.19.0) |
| | Tailwind CSS | **4.3.x** (4.3.3). Browser floor accepted (ADR-0004) |
| | Recharts | **3.10.x** (3.10.1) |
| | MSW | **2.15.x** (2.15.0) |
| Data | PostgreSQL | **16** (16.15). The specification's version is retained |
| | Redis | **7.4** (7.4.11). The specification's major version is retained |
| Optional ML (non-blocking) | Python | **3.14** (3.14.8) |
| | FastAPI | **0.142.x** (0.142.2) |
| | Prophet / LightGBM | Pin only if the ML track starts (latest at verification: 1.4.0 / 4.7.0) |
| Infrastructure | Docker Engine | **29.x** (29.8.2) |
| | Docker Compose | **v5.x** (5.6.0). Omit the obsolete `version:` field |
| | Nginx | **1.30.x stable** (1.30.5) |
| Container images | Backend | `eclipse-temurin:25-jdk` to build, `eclipse-temurin:25-jre` to run (the variant is chosen at Dockerfile time) |
| | Frontend | `node:24` line (the variant is chosen at Dockerfile time) |
| | Database / cache / proxy | `postgres:16.15`, `redis:7.4.11-alpine`, `nginx:1.30.5` |
| | Optional ML | `python:3.14-slim` |
| Tooling | k6 / certbot | Current v2.x / current maintained release when QA and deployment start (v2.3.0 / 5.8.0 at verification) |

**Approval record:**

1. RD-013 and RD-014 were resolved by ADR-0003 and ADR-0004. The Redis 7.4 licensing note in §6 was acknowledged in ADR-0002; no legal conclusion is drawn.
2. The project owner approved the baseline on 2026-10-03 (ADR-0002). The departures from the specification's stated versions are listed in the decision log's departures table (D-01 to D-05).
3. The SSOT index (area 8) and `docs/REQUIRES_DECISION.md` (RD-001, RD-013, RD-014) were updated in the same change.

The master specification is not edited.

CLAUDE.md §J's precondition for scaffolding, an approved baseline, is met. Each scaffolding task still verifies current documentation for the versions it pins (CLAUDE.md rules 5–6). It uses a newer patch within the same line when one exists, and justifies every dependency not listed here (rule 15; ADR-0034).

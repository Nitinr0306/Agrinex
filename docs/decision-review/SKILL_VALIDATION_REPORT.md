# AgriNex — Skill Validation Report

**Status:** REFERENCE, written on 2026-10-04 (Task 8.6). It records how the nine project skills in `.claude/skills/agrinex-*/SKILL.md` were checked against the SSOT, the binding decisions (ADR-0001 to ADR-0042), the architecture and database documents, the API contract, and the security sign-off (ADR-0042).

Skills are tooling. They are never a source of requirements (CLAUDE.md §N). Where a skill and the SSOT disagree, the SSOT wins and the skill is corrected.

## 1. Method

**Inputs:**

- `docs/decision-review/SKILL_INPUTS_AFTER_TASK_8_5.md`;
- the finalized SSOT index, decision log, architecture, database and API documents;
- the security review sign-off.

**Format guidance.** The skill-creator plugin was used for format guidance (YAML frontmatter with `name` and a trigger-rich `description`, an imperative body under 500 lines) and for its frontmatter validator (`quick_validate.py`). Its subagent evaluation and benchmark loop was not run. These skills govern documentation and process rather than producing outputs that could be benchmarked against a baseline, so they were checked by a repository-aware validator and a manual review.

**Automated checks** (`validate_skills.py`, run from the Task 8.6 session workspace). Each skill gets 15 checks:

| # | Check |
|---|---|
| 1 | Frontmatter valid (skill-creator `quick_validate.py`): kebab-case name, description present, no angle brackets, at most 1 024 characters |
| 2 | `name` equals the folder name; LF line endings |
| 3 | Length under 500 lines |
| 4 | The 12 required sections: purpose, when to use, source hierarchy, required reading, non-negotiable rules, common rules, allowed and forbidden responsibilities, dependencies, validation, escalation, output |
| 5 | The 10 common AgriNex rules: read the SSOT first; do not invent requirements, database fields, API contracts or external data; do not bypass ownership or override ADRs; no unrelated changes; human review for security; tests; acceptance criteria |
| 6 | Every referenced file exists. A file the spec names but that is not created yet (role file 02, `scripts/health_check.sh`, `infra/.env.example`) must be marked as planned |
| 7 | Every cited ADR exists and is not PROPOSED |
| 8 | Every operation ID exists in `contracts/openapi.yaml` |
| 9 | Every schema or port name exists in the contract or the decision log |
| 10 | Every code or enumeration value exists in the contract: error codes, field codes, reason codes, audit actions, freshness, trend |
| 11 | Every table or column name exists in the database documents |
| 12 | Every `/api/v1/` endpoint reference is a prefix of a contract path |
| 13 | No stale-decision text: "PROPOSED ADR-0035 to ADR-0042", RD-038 to RD-044 described as open, the old cookie name, "port 8000" |
| 14 | No forbidden path: backend calling ML, ML made mandatory, an alternative data source, scraping, interpolation, microservices, Phase 2 endpoints. A fixed "₹8/km" may appear only as a prohibition |
| 15 | The topical anchors each skill must carry, for example ADR-0030 for architecture, ADR-0039 and ADR-0040 for the contract, RD-037 for cloud |

**Mutation test of the validator** (`mutate_skills.py`). 17 deliberately broken copies, **all 17 detected**:

- a section removed, and a common rule removed;
- a PROPOSED entry cited, and a resolved item described as open;
- "the backend calls the ML service";
- a Phase 2 endpoint;
- an invented table, an invented operation ID, an unknown ADR, a missing file;
- a fixed ₹8/km label, and the old cookie name;
- a name that differs from the folder;
- a fallback data source;
- an invented error code;
- microservices suggested;
- an angle bracket in a description.

**Manual review.** Each skill was read against its sources for AgriNex-specific correctness:

- module list and dependency edges (`COMPONENT_ARCHITECTURE.md` §4.1, ADR-0013, ADR-0039);
- ports (ADR-0035);
- ML boundary (ADR-0030);
- data-source rule (ADR-0005);
- precisions (`DATA_DICTIONARY.md`);
- security rules (ADR-0016, ADR-0017, ADR-0042);
- design approval (ADR-0027);
- transport label (ADR-0038);
- origin (ADR-0039);
- estimate error (ADR-0040);
- operator procedures (ADR-0037, ADR-0041);
- the freeze checklist and acceptance thresholds (spec §10, §48);
- the topology (ADR-0021).

## 2. Results

| Skill | Checks performed | Findings | Corrections | Final status |
|---|---|---|---|---|
| agrinex-architecture | 15 automated (22 file references, 10 ADRs); manual review of edges, ports, ML, phases | None | — | **PASS** |
| agrinex-database | 15 automated (13 file references, 20 ADRs, 3 schema names); manual review of identity, precision, vocabulary, seeds | None | — | **PASS** |
| agrinex-api-contract | 15 automated (18 file references, 19 ADRs, 2 operation IDs); manual review of errors, security, provenance, ML | None | — | **PASS** |
| agrinex-backend | 15 automated (17 file references, 27 ADRs); manual review of baseline, engine rules, security, logging, ML | None | — | **PASS** |
| agrinex-frontend | 15 automated (11 file references, 11 ADRs, 1 operation ID); manual review of states, labels, auth flow, origin, CSP | None | — | **PASS** |
| agrinex-design | 15 automated (7 file references, 16 ADRs); manual review of personas, copy rules, approval workflow | First run: role file 02 path not marked as planned | Marked "not created yet; Task 9 creates it" | **PASS** |
| agrinex-qa | 15 automated (11 file references, 13 ADRs, 1 operation ID); manual review of §48 thresholds, security regression list | None | — | **PASS** |
| agrinex-cloud | 15 automated (13 file references, 12 ADRs); manual review of topology, edge, secrets, backups | First run: `scripts/health_check.sh` not marked as planned. `PING`, `pg_dump` and `pg_isready` flagged as unknown names | Marked the script "named by spec §43, not created yet". The three tool commands were validator false positives, so the validator was corrected to allow them | **PASS** |
| agrinex-release | 15 automated (14 file references, 6 ADRs); manual review of gates against §10, §41, §48, ADR-0042 | First run: `scripts/health_check.sh` not marked as planned; `pg_dump` false positive | Marked the script as created at infrastructure scaffolding. Also: the backup "restore test" is labelled a verification practice, not an SSOT requirement | **PASS** |

**Overall:** all nine skills pass the final run, and all 17 validator mutations are detected.

## 3. Cross-skill consistency confirmed

| Property | Result |
|---|---|
| Old decisions described as unresolved | None. RD-038 to RD-044 appear only as resolved decisions (ADR-0035 to ADR-0042) |
| Contradictory rules between skills | None found: cookie name, origin rule, error codes, ML boundary, operator procedures and transport label are stated the same way everywhere they appear |
| Forbidden dependencies | None. Only the edges of `COMPONENT_ARCHITECTURE.md` §4.1 (with ADR-0039) and the ports of ADR-0035 are named |
| Phase 2 treated as Phase 1 | None. Phase 2 is named only as deferred until the freeze |
| ML optional | Yes. Every skill that mentions ML states that it is optional and database-mediated (ADR-0030) |
| data.gov.in authoritative | Yes. The skills that touch data (architecture, database, backend, cloud, release, qa) cite ADR-0005, and none suggests another source |
| Fabricated-data path | None. Synthetic data is confined to labelled mocks and test fixtures, and demo data to exact exports (ADR-0028) |
| Invented endpoints, tables or features | None. Every operation ID, endpoint prefix, table, code and schema name resolves to the contract or the database documents |

## 4. Notes

- `.claude/skills/agrinex-release/` was not in the Task 8.5 list of eight skills. It was populated in Task 8.6 as instructed.
- `.claude/skills/context7-mcp/SKILL.md` is unchanged. The project skills defer to it for library documentation.
- The agent definitions in `.claude/agents/` were not in scope and are unchanged.
- The validation scripts live in the session workspace, not in the repository, as in Task 8. A later tooling task can add them.

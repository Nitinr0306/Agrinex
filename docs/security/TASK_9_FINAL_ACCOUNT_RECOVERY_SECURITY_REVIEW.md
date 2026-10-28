# AgriNex — Task 9 Final: Account Email and Recovery Security Review

**Status:** REFERENCE (2026-10-04). Security analysis behind ADR-0048, prepared in the Task 9 final decision lock. The Human Project Owner decided the product rules (ADR-0047) and delegated the choice of mechanism (ADR-0043 item 4(b)). This record does not replace the CLAUDE.md rule 16 review: the **implementation** of every item below needs explicit human review before merge.

**Scope:** contract 1.0.0 as revised for ADR-0047 and ADR-0048 (`registerFarmer`, `login`, `requestPasswordReset`, `confirmPasswordReset`, `confirmEmailVerification`, `resendEmailVerification`, `AuthenticatedUser.emailVerified`); the `users` columns of ADR-0048; the Resend integration; `docs/architecture/SECURITY_ARCHITECTURE.md` §4.3 and §4.7. **Not covered:** running code (none exists), deployed configuration, the email provider's own security.

**Baseline unchanged:** BCrypt cost 12; HS256 access tokens of 15 minutes; rotated refresh tokens with family revocation; per-IP and per-identifier rate limits with an in-process fallback; non-revealing authentication semantics (SR-15); no personal data in logs (ADR-0042); the SR-01 to SR-16 outcomes (ADR-0042).

**Reference:** OWASP Cheat Sheet Series, "Forgot Password Cheat Sheet" (fetched 2026-10-04). Its recommendations are quoted in ADR-0048.

## Findings and treatment

| ID | Threat | Treatment | Status |
|---|---|---|---|
| AR-01 | Account enumeration through the reset request (body or timing) | Always 202 with no body; the email is sent after the response, off the request path; unknown identifiers count against the per-identifier limit exactly like known ones | Mitigated (ADR-0048 item 5) |
| AR-02 | Registration 409 reveals that an email is registered | The same class of risk as SR-01 (accepted by the owner for phone numbers, ADR-0042): unique identifiers force it without OTP. Bounded by the register limit (10/min per IP); an email claimed by someone else is freed by the erasure procedure (ADR-0037) | Residual, decided under ADR-0048; recorded for the owner's awareness |
| AR-03 | Recovery is only as strong as the user's mailbox, for FARMER and ADMIN alike | Reset links go only to a verified email of an active account; 30-minute single-use links; a successful reset revokes every refresh-token family and sends a "password changed" notice; no separate password system (H5). ADMIN mailboxes are operator-controlled (bootstrap, ADR-0028) | Residual, inherent to email recovery; decided under ADR-0048 |
| AR-04 | Token leakage through logs, `Referer`, browser history or analytics | Token in the URL fragment (never sent to a server); the page removes it with `history.replaceState`; `Referrer-Policy: no-referrer` on the reset page; tokens and links never logged; only SHA-256 hashes stored | Mitigated (ADR-0048 items 1, 2, 10) |
| AR-05 | Guessing or brute-forcing a token | 256 random bits from a cryptographically secure generator; 10 confirmations per minute per IP; tokens expire (24 hours, 30 minutes) and are single-use | Mitigated |
| AR-06 | Email squatting: someone registers with another person's email, which stays unverified | The real owner cannot register that email; they can still reset nothing (it is unverified). The verification email lets the owner ignore it; the erasure procedure frees the email on request | Residual, low impact; decided under ADR-0048 |
| AR-07 | An unverified external API dependency (spec §39) | Resend's API is verified from its official documentation (Context7, 2026-10-04). A live send from the deployment configuration must succeed before the auth module relies on it (§49 VERIFIED); production needs a verified sending domain on the project domain (RD-037) | **Implementation gate** |
| AR-08 | Email bombing through reset requests or resends | 3 reset requests per identifier per hour; 3 resends per account per hour; 10 per minute per IP | Mitigated |
| AR-09 | Sessions surviving a reset | Every refresh-token family is revoked and the cookie cleared; an outstanding access token can live up to 15 minutes, the stateless model the owner accepted (SR-03) | Mitigated within the SR-03 model |
| AR-10 | Host-header injection into reset links | Links are built only from the configured public origin, never from the request's `Host` | Mitigated |
| AR-11 | Linking a phone number to an email through limit behaviour | The two identifiers are counted separately by every limit. Consequence: an attacker may make up to 10 failed logins per identifier per 15 minutes, so 20 per account through both identifiers; BCrypt and the per-IP limit still apply | Mitigated; minor residual recorded |
| AR-12 | Exposure of the email provider key | `RESEND_API_KEY` only in the backend environment (`.env`, mode 600; ADR-0020); never in the frontend, logs, responses or documentation; never readable through an API | Mitigated |

## Implementation checks for the rule 16 review

The reviewer confirms, in code and tests:

1. The reset request's response and timing do not depend on the account (test with unknown, unverified, inactive and eligible accounts).
2. Tokens: CSPRNG, 32 bytes, base64url; only SHA-256 hashes persisted; single use; expiry enforced; a new link replaces the old one.
3. Reset: password rules per role, BCrypt cost 12, all refresh families revoked in the same transaction, cookie cleared, notice sent, no session issued.
4. Links: configured origin; token only in the fragment; reset page with `Referrer-Policy: no-referrer`.
5. Limits: values, unknown-identifier counting, separate identifier counting, in-process fallback.
6. Logs: no email address, token, link or email body anywhere.
7. Resend: API key from the environment only; bounded timeout; failure logged without personal data; no retry loop; live send verified (AR-07).

## Sign-off

| Item | Outcome | By | Date |
|---|---|---|---|
| Product rules (email at registration, login by email or phone, recovery through verified email, no SMS/OTP) | Decided | Human Project Owner (ADR-0047) | 2026-10-04 |
| Mechanism (this review) | Decided under the owner's delegation | ADR-0048 (delegation in ADR-0043 item 4(b)) | 2026-10-04 |
| Implementation | Pending: human review before merge (CLAUDE.md rule 16) | — | — |

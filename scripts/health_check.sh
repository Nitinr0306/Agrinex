#!/usr/bin/env sh
# AgriNex quick deployment validation (spec §43 "health_check.sh", §45; ADR-0017, ADR-0021 item 7).
# Run on the deployment host from the repository root:  scripts/health_check.sh https://agrinex.example
# It reads only public endpoints and the backend readiness inside the Docker network; it changes nothing.
set -u

ORIGIN="${1:?usage: scripts/health_check.sh https://<public host>}"
HOST="${ORIGIN#https://}"
failures=0

pass() { printf 'PASS  %s\n' "$1"; }
fail() { printf 'FAIL  %s\n' "$1"; failures=$((failures + 1)); }

status() { curl -s -o /dev/null -w '%{http_code}' --max-time 20 "$@"; }

# 1. Containers healthy (Compose health checks of ADR-0021 item 7).
for service in agrinex-db agrinex-redis agrinex-backend agrinex-frontend agrinex-nginx; do
  state=$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' \
    "$(docker compose ps -q "$service" 2>/dev/null)" 2>/dev/null)
  case "$state" in
    healthy|running) pass "$service is $state" ;;
    *) fail "$service is ${state:-missing}" ;;
  esac
done

# 2. Backend readiness inside the network only (never published).
if docker compose exec -T agrinex-backend curl -fsS http://localhost:8080/actuator/health/readiness | grep -q '"UP"'; then
  pass "backend readiness UP (internal)"
else
  fail "backend readiness (internal)"
fi

# 3. The public edge.
[ "$(status "http://$HOST/")" = "301" ] && pass "HTTP redirects to HTTPS" || fail "HTTP redirect"
curl -fsS --max-time 20 "$ORIGIN/api/health" | grep -q '"ok"' && pass "frontend /api/health ok" || fail "frontend /api/health"
[ "$(status "$ORIGIN/")" = "200" ] && pass "home page 200" || fail "home page"
[ "$(status "$ORIGIN/api/v1/geography/states")" = "200" ] && pass "API /api/v1/geography/states 200" || fail "API states"
[ "$(status "$ORIGIN/actuator/health")" = "404" ] && pass "/actuator/ answers 404 at the edge" || fail "/actuator/ not blocked"
[ "$(status "$ORIGIN/api/v1/admin/ingestion/health")" = "401" ] && pass "admin API requires a token (401)" || fail "admin API without token"
headers=$(curl -sS -D - -o /dev/null --max-time 20 "$ORIGIN/")
for header in strict-transport-security x-content-type-options x-frame-options referrer-policy content-security-policy; do
  printf '%s' "$headers" | grep -qi "^$header:" && pass "header $header" || fail "header $header"
done

if [ "$failures" -eq 0 ]; then
  echo "All checks passed."
else
  echo "$failures check(s) failed."
fi
exit "$failures"

#!/usr/bin/env bash
# Changes ONE value in the production environment file /opt/agrinex/.env (ADR-0020 item 4) without ever displaying
# it: the new value is typed at a hidden prompt (or piped in), the file is rewritten atomically and stays mode 600.
# It does not restart anything and does not revoke the previous credential at its provider; DEPLOYMENT_GUIDE.md
# section 9 gives the whole procedure.
#
# Usage, on the server, from /opt/agrinex:
#   bash infra/deploy/set_env_value.sh DATA_GOV_API_KEY
#   bash infra/deploy/set_env_value.sh --clear AGRINEX_BOOTSTRAP_ADMIN_PASSWORD
# Only these variables can be changed here; the generated database, Redis and JWT secrets cannot, because changing
# them needs a coordinated change inside PostgreSQL or signs every user out.
set -euo pipefail
umask 077

cd "$(dirname "$0")/../.."
target=".env"
allowed="DATA_GOV_API_KEY RESEND_API_KEY AGRINEX_EMAIL_FROM AGRINEX_BOOTSTRAP_ADMIN_EMAIL AGRINEX_BOOTSTRAP_ADMIN_PASSWORD"

clear=false
if [ "${1:-}" = "--clear" ]; then
  clear=true
  shift
fi
name="${1:?usage: bash infra/deploy/set_env_value.sh [--clear] <VARIABLE>}"
case " $allowed " in
  *" $name "*) ;;
  *) echo "$name cannot be changed with this script (allowed: $allowed)" >&2; exit 1 ;;
esac
[ -f "$target" ] || { echo "$(pwd)/$target does not exist" >&2; exit 1; }

value=""
if [ "$clear" = false ]; then
  if [ -t 0 ]; then
    read -rsp "New value for $name (not displayed): " value
    echo
  else
    IFS= read -r value || true
  fi
  value="${value%$'\r'}"
  [ -n "$value" ] || { echo "Empty value; nothing was changed (use --clear to empty a variable)" >&2; exit 1; }
fi

tmp="$(mktemp "$target.XXXXXX")"
trap 'rm -f "$tmp"' EXIT
found=false
while IFS= read -r line || [ -n "$line" ]; do
  if [[ "$line" == "$name="* ]]; then
    printf '%s=%s\n' "$name" "$value" >> "$tmp"
    found=true
  else
    printf '%s\n' "$line" >> "$tmp"
  fi
done < "$target"
[ "$found" = true ] || printf '%s=%s\n' "$name" "$value" >> "$tmp"
chmod 600 "$tmp"
mv "$tmp" "$target"
trap - EXIT

if [ "$clear" = true ]; then
  echo "$name is now empty in $(pwd)/$target (mode $(stat -c '%a' "$target"))."
else
  echo "$name was changed in $(pwd)/$target (mode $(stat -c '%a' "$target")). The value was not displayed."
fi
echo "Apply it by recreating the backend only:  docker compose up -d --no-deps --force-recreate agrinex-backend"

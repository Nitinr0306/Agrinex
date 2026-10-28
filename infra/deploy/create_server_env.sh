#!/usr/bin/env bash
# Creates the production environment file /opt/agrinex/.env ONCE, on the server (ADR-0020 item 4, ADR-0055), from
# infra/deploy/production.env.example:
#   - POSTGRES_PASSWORD, REDIS_PASSWORD and JWT_SECRET are generated here with openssl, so they never exist anywhere
#     else and differ from every local value;
#   - DATA_GOV_API_KEY, RESEND_API_KEY and AGRINEX_EMAIL_FROM are read from the owner's prod.env, uploaded for this
#     step only; the uploaded copy is deleted afterwards.
# The file is written with mode 600 and no value is ever printed. It refuses to overwrite an existing .env: changing
# a database password later needs a coordinated change inside PostgreSQL too.
#
# Usage, on the server, from /opt/agrinex:  bash infra/deploy/create_server_env.sh /tmp/prod.env
set -euo pipefail
umask 077

cd "$(dirname "$0")/../.."
template="infra/deploy/production.env.example"
target=".env"
owner_file="${1:?usage: bash infra/deploy/create_server_env.sh <path of the uploaded prod.env>}"

[ -f "$template" ] || { echo "missing $template" >&2; exit 1; }
[ -f "$owner_file" ] || { echo "missing the owner's file $owner_file" >&2; exit 1; }
if [ -e "$target" ]; then
  echo "$target already exists in $(pwd); nothing was changed" >&2
  exit 1
fi
command -v openssl >/dev/null || { echo "openssl is required" >&2; exit 1; }

# The value of NAME in the owner's file, without surrounding quotes or a trailing carriage return (Windows editors).
owner_value() {
  sed -n "s/^$1=//p" "$owner_file" | head -n 1 | tr -d '\r' | sed -e 's/^"\(.*\)"$/\1/' -e "s/^'\(.*\)'$/\1/"
}

declare -A values
values[POSTGRES_PASSWORD]="$(openssl rand -hex 32)"
values[REDIS_PASSWORD]="$(openssl rand -hex 32)"
values[JWT_SECRET]="$(openssl rand -base64 32 | tr -d '\n')"
for name in DATA_GOV_API_KEY RESEND_API_KEY AGRINEX_EMAIL_FROM; do
  values[$name]="$(owner_value "$name")"
  if [ -z "${values[$name]}" ]; then
    echo "$name is missing in $owner_file; nothing was written" >&2
    exit 1
  fi
done

tmp="$(mktemp "$target.XXXXXX")"
trap 'rm -f "$tmp"' EXIT
while IFS= read -r line || [ -n "$line" ]; do
  name="${line%%=*}"
  if [[ "$line" != \#* && "$line" == *=* && -n "${values[$name]+set}" ]]; then
    printf '%s=%s\n' "$name" "${values[$name]}" >> "$tmp"
  else
    printf '%s\n' "$line" >> "$tmp"
  fi
done < "$template"
chmod 600 "$tmp"
mv "$tmp" "$target"
trap - EXIT

# The uploaded owner file has served its purpose.
shred -u "$owner_file" 2>/dev/null || rm -f "$owner_file"

echo "Created $(pwd)/$target (mode $(stat -c '%a' "$target"), owner $(stat -c '%U' "$target"))."
echo "Generated here: POSTGRES_PASSWORD, REDIS_PASSWORD, JWT_SECRET. From the owner's file: DATA_GOV_API_KEY,"
echo "RESEND_API_KEY, AGRINEX_EMAIL_FROM. The uploaded owner file was deleted. No value was displayed."

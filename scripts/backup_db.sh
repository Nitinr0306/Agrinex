#!/usr/bin/env sh
# AgriNex daily database backup on the deployment host (ADR-0021 item 6): a custom-format pg_dump of the running
# agrinex-db container, keeping 7 days. Run from the repository root, normally by cron (DEPLOYMENT_GUIDE.md):
#   scripts/backup_db.sh
# Backups stay on the same VM; copying them off the VM is an owner action (ADR-0055 item 4).
set -eu

cd "$(dirname "$0")/.."
BACKUP_DIR="${AGRINEX_BACKUP_DIR:-/var/backups/agrinex}"
KEEP_DAYS=7

umask 077
mkdir -p "$BACKUP_DIR"
target="$BACKUP_DIR/agrinex-$(date -u +%Y%m%dT%H%M%SZ).dump"

# The credentials stay inside the container's environment; nothing is printed.
docker compose exec -T agrinex-db sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc' > "$target.partial"
mv "$target.partial" "$target"

# Only backups older than the retention are removed; the database is never touched.
find "$BACKUP_DIR" -maxdepth 1 -name 'agrinex-*.dump' -mtime +$((KEEP_DAYS - 1)) -delete
echo "Backup written: $target ($(du -h "$target" | cut -f1))"

#!/usr/bin/env sh
# certbot deploy hook (ADR-0021 item 4): after a certificate is renewed, reload Nginx so that it serves the new one.
# Installed by DEPLOYMENT_GUIDE.md as /etc/letsencrypt/renewal-hooks/deploy/agrinex-reload-nginx.sh.
set -eu
cd /opt/agrinex
docker compose exec -T agrinex-nginx nginx -s reload

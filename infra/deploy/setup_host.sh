#!/usr/bin/env bash
# One-time setup of the AgriNex host (ADR-0021, ADR-0055): Oracle Cloud Always Free VM, Canonical Ubuntu 24.04,
# Arm64. Run once as the default "ubuntu" user, which has sudo:  bash infra/deploy/setup_host.sh
# Every step can be re-run safely. It installs Docker Engine with the Compose plugin from Docker's apt repository
# (docs.docker.com "Install Docker Engine on Ubuntu"), certbot, opens ports 80 and 443 in the host firewall, and
# creates the directories the stack mounts. It never touches data.
set -euo pipefail

# 1. Docker Engine and the Compose plugin (official apt repository).
if ! command -v docker >/dev/null 2>&1; then
  sudo apt-get update
  sudo apt-get install -y ca-certificates curl
  sudo install -m 0755 -d /etc/apt/keyrings
  sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
  sudo chmod a+r /etc/apt/keyrings/docker.asc
  sudo tee /etc/apt/sources.list.d/docker.sources >/dev/null <<EOF
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}")
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
EOF
  sudo apt-get update
  sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
fi
sudo usermod -aG docker "$USER"

# 2. certbot on the host (ADR-0021 item 4); the package installs its own renewal timer.
sudo apt-get install -y certbot

# 3. Oracle's Ubuntu images reject inbound traffic other than SSH in iptables, in addition to the VCN security
#    list. Accept 80 and 443 before the reject rule, and keep the rules across reboots.
for port in 80 443; do
  if ! sudo iptables -C INPUT -p tcp -m state --state NEW --dport "$port" -j ACCEPT 2>/dev/null; then
    sudo iptables -I INPUT 5 -p tcp -m state --state NEW --dport "$port" -j ACCEPT
  fi
done
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y iptables-persistent
sudo netfilter-persistent save

# 4. Directories the stack and the operator procedures use.
sudo mkdir -p /var/www/certbot /var/backups/agrinex /opt/agrinex
sudo chown "$USER":"$USER" /opt/agrinex
sudo chmod 700 /var/backups/agrinex
sudo chown "$USER":"$USER" /var/backups/agrinex

echo "Host ready. Log out and back in once so that the docker group applies."

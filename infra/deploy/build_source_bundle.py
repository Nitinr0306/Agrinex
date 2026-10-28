"""Builds the source bundle that is copied to the AgriNex server (ADR-0055; docs/operations/DEPLOYMENT_GUIDE.md).

Only an explicit allow-list leaves this machine: what the two images and the stack need to be built and run on the
server. Files ignored by Git (.env files, .mcp.json, local-reference-data, build output) are never candidates, and
the deny-list below removes tests, tooling and anything else not needed on a server. Before the archive is written,
every selected file is checked for forbidden names and for the secret values of infra/.env and .mcp.json; only
their names are ever printed. The archive and its manifest (path, size, SHA-256) go to an output folder outside the
repository.

Usage, from the repository root:  python infra/deploy/build_source_bundle.py [output folder]
On the server:  tar -xzf agrinex-source.tgz -C /opt/agrinex && (cd /opt/agrinex && sha256sum -c MANIFEST.sha256)
"""

from __future__ import annotations

import hashlib
import io
import json
import pathlib
import re
import subprocess
import sys
import tarfile
import tempfile

ROOT = pathlib.Path(__file__).resolve().parents[2]

# What the server needs: the image sources, the stack, the edge, the operator scripts and the runbook.
ALLOW = (
    "docker-compose.yml",
    "backend/Dockerfile", "backend/.dockerignore", "backend/pom.xml", "backend/mvnw",
    "backend/.mvn/", "backend/src/main/",
    "frontend/Dockerfile", "frontend/.dockerignore", "frontend/package.json", "frontend/package-lock.json",
    "frontend/next.config.ts", "frontend/proxy.ts", "frontend/tsconfig.json", "frontend/next-env.d.ts",
    "frontend/postcss.config.mjs", "frontend/app/", "frontend/components/", "frontend/lib/", "frontend/public/",
    "frontend/__mocks__/",
    "infra/nginx/templates/", "infra/deploy/",
    "scripts/health_check.sh", "scripts/backup_db.sh",
    "docs/operations/DEPLOYMENT_GUIDE.md",
)

# Never on the server, even when inside an allowed folder.
DENY = (
    re.compile(r"(^|/)\.env($|\.)"), re.compile(r"\.env$"), re.compile(r"(^|/)\.mcp\.json$"),
    re.compile(r"\.(dump|sql\.gz|pem|key|p12|jks)$"), re.compile(r"(^|/)(node_modules|\.next|target)/"),
    re.compile(r"\.test\.(ts|tsx)$"), re.compile(r"(^|/)(e2e|test-results|playwright-report)/"),
    re.compile(r"^infra/deploy/build_source_bundle\.py$"),
)

TOKEN_PATTERNS = {
    "resend-like key": re.compile(r"\bre_[A-Za-z0-9]{20,}"),
    "github token": re.compile(r"\bgh[pousr]_[A-Za-z0-9]{30,}"),
    "private key": re.compile(r"-----BEGIN [A-Z ]*PRIVATE KEY-----"),
}


def candidates() -> list[str]:
    """Files Git would consider (tracked or untracked), never the ones .gitignore excludes."""
    output = subprocess.run(["git", "ls-files", "--cached", "--others", "--exclude-standard", "-z"], cwd=ROOT,
                            check=True, capture_output=True).stdout.decode("utf-8")
    return sorted(path for path in output.split("\0") if path)


def selected(path: str) -> bool:
    allowed = any(path == entry or (entry.endswith("/") and path.startswith(entry)) for entry in ALLOW)
    return allowed and not any(pattern.search(path) for pattern in DENY)


def secret_values() -> dict[str, str]:
    """Secret values known on this machine, by name; used only to make sure none of them leaves it."""
    secrets: dict[str, str] = {}
    env = ROOT / "infra" / ".env"
    if env.is_file():
        for line in env.read_text(encoding="utf-8").splitlines():
            if "=" in line and not line.lstrip().startswith("#"):
                name, value = line.split("=", 1)
                value = value.strip().strip('"')
                if any(word in name for word in ("KEY", "SECRET", "PASSWORD", "TOKEN")) and len(value) >= 8:
                    secrets[name.strip()] = value

    def walk(node, path=""):
        if isinstance(node, dict):
            for key, value in node.items():
                yield from walk(value, f"{path}.{key}")
        elif isinstance(node, list):
            for index, value in enumerate(node):
                yield from walk(value, f"{path}[{index}]")
        elif isinstance(node, str) and len(node) >= 16 and not node.startswith("${"):
            yield path, node

    mcp = ROOT / ".mcp.json"
    if mcp.is_file():
        for path, value in walk(json.loads(mcp.read_text(encoding="utf-8"))):
            if any(word in path.lower() for word in ("key", "token", "secret", "auth", "header", "env")):
                secrets[".mcp.json" + path] = value
    return secrets


def main() -> int:
    output = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(tempfile.gettempdir()) / "agrinex-bundle"
    if ROOT in output.resolve().parents or output.resolve() == ROOT:
        print("The output folder must be outside the repository.", file=sys.stderr)
        return 2
    files = [path for path in candidates() if selected(path) and (ROOT / path).is_file()]
    secrets = secret_values()
    problems = []
    for path in files:
        text = (ROOT / path).read_bytes().decode("utf-8", errors="ignore")
        problems += [f"{name} value in {path}" for name, value in secrets.items() if value in text]
        problems += [f"{name} pattern in {path}" for name, pattern in TOKEN_PATTERNS.items() if pattern.search(text)]
    for required in ("docker-compose.yml", "backend/Dockerfile", "frontend/Dockerfile",
                     "infra/nginx/templates/default.conf.template", "infra/deploy/production.env.example"):
        if required not in files:
            problems.append(f"required file missing from the bundle: {required}")
    if problems:
        print("Bundle NOT written:", *problems, sep="\n  ", file=sys.stderr)
        return 1

    output.mkdir(parents=True, exist_ok=True)
    manifest = []
    with tarfile.open(output / "agrinex-source.tgz", "w:gz") as archive:
        for path in files:
            data = (ROOT / path).read_bytes()
            manifest.append(f"{hashlib.sha256(data).hexdigest()}  {path}")
            info = tarfile.TarInfo(path)
            info.size = len(data)
            info.mode = 0o755 if path.endswith((".sh", "mvnw")) else 0o644
            archive.addfile(info, io.BytesIO(data))
        listing = ("\n".join(manifest) + "\n").encode("utf-8")
        info = tarfile.TarInfo("MANIFEST.sha256")
        info.size = len(listing)
        info.mode = 0o644
        archive.addfile(info, io.BytesIO(listing))
    (output / "MANIFEST.sha256").write_text("\n".join(manifest) + "\n", encoding="utf-8", newline="\n")
    size = (output / "agrinex-source.tgz").stat().st_size
    print(f"{len(files)} files, {size / 1e6:.1f} MB: {output / 'agrinex-source.tgz'}")
    print(f"Manifest: {output / 'MANIFEST.sha256'}. Checked against {len(secrets)} secret values: none found.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

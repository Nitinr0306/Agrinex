"""Job configuration from the deployment environment (ADR-0020, ADR-0021).

The job connects as the restricted ``agrinex_ml`` role (ADR-0030 item 1). Its credentials come only from the
environment; they are never logged, written or passed on the command line.
"""

from __future__ import annotations

import os
from dataclasses import dataclass, field
from pathlib import Path


@dataclass(frozen=True, slots=True)
class DatabaseSettings:
    host: str
    port: int
    dbname: str
    user: str
    password: str = field(repr=False)

    @classmethod
    def from_environment(cls, environ: dict[str, str] | None = None) -> DatabaseSettings:
        env = os.environ if environ is None else environ
        missing = [name for name in ("POSTGRES_DB", "ML_DB_USER", "ML_DB_PASSWORD") if not env.get(name)]
        if missing:
            raise ValueError("Missing environment variables: " + ", ".join(missing))
        return cls(host=env.get("POSTGRES_HOST", "localhost"), port=int(env.get("POSTGRES_PORT", "5432")),
                   dbname=env["POSTGRES_DB"], user=env["ML_DB_USER"], password=env["ML_DB_PASSWORD"])

    def connection_arguments(self) -> dict[str, str | int]:
        return {"host": self.host, "port": self.port, "dbname": self.dbname, "user": self.user,
                "password": self.password}


def default_contracts_dir() -> Path:
    """The repository's contracts/ directory, unless AGRINEX_CONTRACTS_DIR names another one (for an image)."""
    configured = os.environ.get("AGRINEX_CONTRACTS_DIR")
    if configured:
        return Path(configured)
    return Path(__file__).resolve().parents[4] / "contracts"

"""Command line of the forecasting job.

    python -m agrinex_forecasting --model naive-last-value [--validation-start 2026-08-01
        --validation-end 2026-08-31] [--write]

Without ``--write`` the job evaluates and reports only. The operator runs it only after the HARD RULE 2 gate and
the spec section 35 criteria are met (docs/api/ML_CONTRACT_BOUNDARY.md section 8).
"""

from __future__ import annotations

import argparse
import logging
import sys
from datetime import date
from pathlib import Path

import psycopg

from agrinex_forecasting.evaluation import ValidationWindow
from agrinex_forecasting.job import ForecastJob
from agrinex_forecasting.models import MODELS
from agrinex_forecasting.output import ContractValidator
from agrinex_forecasting.repository import ForecastWriter, SeriesReader
from agrinex_forecasting.settings import DatabaseSettings, default_contracts_dir


def main(argv: list[str] | None = None) -> int:
    arguments = _parser().parse_args(argv)
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s %(message)s")
    if (arguments.validation_start is None) != (arguments.validation_end is None):
        print("--validation-start and --validation-end must be given together", file=sys.stderr)
        return 2
    window = (ValidationWindow(arguments.validation_start, arguments.validation_end)
              if arguments.validation_start else None)
    settings = DatabaseSettings.from_environment()
    validator = ContractValidator(arguments.contracts_dir or default_contracts_dir())
    with psycopg.connect(**settings.connection_arguments()) as reading, \
            psycopg.connect(**settings.connection_arguments(), autocommit=True) as writing:
        writer = ForecastWriter(writing) if arguments.write else None
        job = ForecastJob(MODELS[arguments.model], validator, writer, window)
        report = job.run(SeriesReader(reading).series())
    print(report)
    return 0 if report.generations_rejected == 0 else 1


def _parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(prog="agrinex-forecasting", description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--model", required=True, choices=sorted(MODELS))
    parser.add_argument("--validation-start", type=date.fromisoformat)
    parser.add_argument("--validation-end", type=date.fromisoformat)
    parser.add_argument("--contracts-dir", type=Path)
    parser.add_argument("--write", action="store_true", help="write the generations to the forecasts table")
    return parser

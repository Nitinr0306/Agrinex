"""The forecasting job (spec section 35; ADR-0030).

For every series with at least 90 observations it scores the chosen model on the validation window, when one is
given, then forecasts each horizon from the whole series and writes one generation per horizon. Nothing is
written unless ``write`` is requested: by default the job only evaluates and reports.
"""

from __future__ import annotations

import logging
from collections.abc import Iterable
from dataclasses import dataclass, field
from datetime import datetime, timedelta, timezone
from decimal import Decimal
from typing import Protocol

from agrinex_forecasting import __version__
from agrinex_forecasting.evaluation import ValidationWindow, evaluate
from agrinex_forecasting.models import ForecastModel
from agrinex_forecasting.output import HORIZONS, ContractValidator, ForecastOutput, ForecastPoint, metric, price
from agrinex_forecasting.repository import MixedUnitError
from agrinex_forecasting.series import DailySeries

log = logging.getLogger(__name__)


class GenerationWriter(Protocol):
    def write(self, output: ForecastOutput) -> None: ...


@dataclass(slots=True)
class JobReport:
    """What one run did, with the validation MAE of each scored series, per horizon."""

    series_read: int = 0
    series_eligible: int = 0
    series_skipped_mixed_units: int = 0
    generations_written: int = 0
    generations_rejected: int = 0
    mae_by_horizon: dict[int, list[Decimal]] = field(default_factory=dict)


@dataclass(frozen=True, slots=True)
class ForecastJob:
    model: ForecastModel
    validator: ContractValidator
    writer: GenerationWriter | None
    window: ValidationWindow | None = None

    def run(self, series: Iterable[DailySeries | MixedUnitError], generated_at: datetime | None = None) -> JobReport:
        report = JobReport()
        when = generated_at or datetime.now(timezone.utc)
        for item in series:
            report.series_read += 1
            if isinstance(item, MixedUnitError):
                report.series_skipped_mixed_units += 1
                log.warning("Series skipped: %s", item)
                continue
            if not item.eligible():
                continue
            report.series_eligible += 1
            for horizon in HORIZONS:
                self._forecast(item, horizon, when, report)
        log.info("Forecast job finished: %s", report)
        return report

    def _forecast(self, series: DailySeries, horizon: int, generated_at: datetime, report: JobReport) -> None:
        metrics = evaluate(self.model, series, horizon, self.window) if self.window else None
        if metrics is not None and metrics.mae is not None:
            report.mae_by_horizon.setdefault(horizon, []).append(metrics.mae)
        target = series.last_date + timedelta(days=horizon)
        output = ForecastOutput(
            market_id=series.market_id, commodity_id=series.commodity_id, forecast_horizon_days=horizon,
            forecast=(ForecastPoint(target, price(self.model.predict(series.points, target)), None, None),),
            model_name=self.model.name, model_version=__version__,
            mae=metric(metrics.mae) if metrics else None, rmse=metric(metrics.rmse) if metrics else None,
            training_data_through=series.last_date, generated_at=generated_at)
        problems = self.validator.problems(output)
        if problems:
            report.generations_rejected += 1
            log.error("Generation rejected by the output contract: %s", "; ".join(problems))
            return
        if self.writer is not None:
            self.writer.write(output)
            report.generations_written += 1

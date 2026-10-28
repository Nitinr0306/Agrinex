"""The spec section 35 baseline models, always built first.

1. Naive last-value: the forecast for any later date is the last observed modal price.
2. 7-day rolling mean: the mean of the modal prices observed in the 7 calendar days ending on the last
   observation date (the same calendar-window reading as the trend windows of ADR-0025).

Both use exact decimal arithmetic and produce no prediction bounds: a bound is never invented
(docs/api/ML_CONTRACT_BOUNDARY.md section 4). ARIMA, Prophet and LightGBM are not built until the ML track
starts and real data justifies them (spec section 35; technology baseline).
"""

from __future__ import annotations

from collections.abc import Sequence
from datetime import date, timedelta
from decimal import Decimal
from typing import Protocol

from agrinex_forecasting.series import DailyPoint


class ForecastModel(Protocol):
    """A model that predicts the modal price of a later date from the history it is given."""

    name: str

    def predict(self, history: Sequence[DailyPoint], target: date) -> Decimal:
        """The predicted modal price for ``target``, using only ``history`` (no leakage)."""
        ...


class NaiveLastValue:
    """Spec section 35 baseline 1: predict the last observed value."""

    name = "naive-last-value"

    def predict(self, history: Sequence[DailyPoint], target: date) -> Decimal:
        _require_history(history, target)
        return history[-1].modal_price


class RollingMean:
    """Spec section 35 baseline 2: the mean of the values in the last ``window_days`` calendar days."""

    def __init__(self, window_days: int = 7) -> None:
        if window_days < 1:
            raise ValueError("The window must cover at least one day")
        self.window_days = window_days
        self.name = f"rolling-mean-{window_days}d"

    def predict(self, history: Sequence[DailyPoint], target: date) -> Decimal:
        _require_history(history, target)
        first_day = history[-1].date - timedelta(days=self.window_days - 1)
        values = [point.modal_price for point in history if point.date >= first_day]
        return sum(values, Decimal(0)) / Decimal(len(values))


#: The models the job can run, by name.
MODELS: dict[str, ForecastModel] = {model.name: model for model in (NaiveLastValue(), RollingMean(7))}


def _require_history(history: Sequence[DailyPoint], target: date) -> None:
    if not history:
        raise ValueError("A forecast needs at least one observation")
    if target <= history[-1].date:
        raise ValueError("A forecast is for a date after the last observation")

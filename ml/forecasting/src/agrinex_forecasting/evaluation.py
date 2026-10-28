"""Chronological evaluation (spec section 35 "Validation Strategy"; section 47 Q14).

Expanding-window evaluation over a validation window: for each origin date in the window, the model sees only
the points dated on or before the origin and predicts the value ``horizon_days`` later; the prediction is
scored when an observation exists on that date. No random split and no leakage. Metrics: MAE, RMSE and
directional accuracy (did the forecast move in the same direction as the observed price?).
"""

from __future__ import annotations

from dataclasses import dataclass
from datetime import date, timedelta
from decimal import Decimal

from agrinex_forecasting.models import ForecastModel
from agrinex_forecasting.series import DailySeries


@dataclass(frozen=True, slots=True)
class ValidationWindow:
    """Inclusive range of origin dates, for example August for a January-July training period (spec section 35)."""

    start: date
    end: date

    def __post_init__(self) -> None:
        if self.end < self.start:
            raise ValueError("The validation window ends before it starts")


@dataclass(frozen=True, slots=True)
class Metrics:
    """Errors in the series' price unit; ``None`` when no prediction could be scored."""

    evaluated_points: int
    mae: Decimal | None
    rmse: Decimal | None
    directional_accuracy: Decimal | None


def evaluate(model: ForecastModel, series: DailySeries, horizon_days: int, window: ValidationWindow) -> Metrics:
    errors: list[Decimal] = []
    directions_correct = 0
    for origin in series.points:
        target = origin.date + timedelta(days=horizon_days)
        if not window.start <= origin.date <= window.end:
            continue
        actual = series.point_on(target)
        if actual is None:
            continue
        predicted = model.predict(series.history_through(origin.date), target)
        errors.append(predicted - actual.modal_price)
        if _sign(predicted - origin.modal_price) == _sign(actual.modal_price - origin.modal_price):
            directions_correct += 1
    if not errors:
        return Metrics(0, None, None, None)
    count = Decimal(len(errors))
    mae = sum((abs(error) for error in errors), Decimal(0)) / count
    rmse = (sum((error * error for error in errors), Decimal(0)) / count).sqrt()
    return Metrics(len(errors), mae, rmse, Decimal(directions_correct) / count)


def _sign(value: Decimal) -> int:
    return (value > 0) - (value < 0)

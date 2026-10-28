"""The input contract DailyPriceSeries (docs/api/ML_CONTRACT_BOUNDARY.md section 3).

One point per date, chosen in the database view by the ADR-0010 selection rules: a real observation, never an
average and never interpolated. Dates without data are absent.
"""

from __future__ import annotations

from dataclasses import dataclass
from datetime import date
from decimal import Decimal
from uuid import UUID

#: Spec section 35, criterion 1, and ADR-0030 item 5: train only on series with at least 90 observations.
MIN_OBSERVATIONS = 90


@dataclass(frozen=True, slots=True)
class DailyPoint:
    """One selected observation of a date, in the series' price unit."""

    date: date
    modal_price: Decimal
    min_price: Decimal | None
    max_price: Decimal | None


@dataclass(frozen=True, slots=True)
class DailySeries:
    """The representative daily series of one market and commodity, in ascending date order."""

    market_id: UUID
    commodity_id: UUID
    price_unit: str
    points: tuple[DailyPoint, ...]

    def __post_init__(self) -> None:
        dates = [point.date for point in self.points]
        if dates != sorted(set(dates)):
            raise ValueError("A daily series has one point per date, in ascending order")

    @property
    def last_date(self) -> date:
        return self.points[-1].date

    def eligible(self) -> bool:
        """Whether the series has enough observations to train on (spec section 35)."""
        return len(self.points) >= MIN_OBSERVATIONS

    def history_through(self, last: date) -> tuple[DailyPoint, ...]:
        """The points dated on or before ``last``: what a model may see when forecasting from ``last``."""
        return tuple(point for point in self.points if point.date <= last)

    def point_on(self, day: date) -> DailyPoint | None:
        return next((point for point in self.points if point.date == day), None)

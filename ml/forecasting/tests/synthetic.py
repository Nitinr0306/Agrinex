"""SYNTHETIC TEST DATA ONLY.

Hand-made series for unit tests of the forecasting code. They are not government-reported observations, are
never written to a database and must never be presented as real data (Task 10 instruction; spec P10).
"""

from __future__ import annotations

from datetime import date, timedelta
from decimal import Decimal
from uuid import UUID

from agrinex_forecasting.series import DailyPoint, DailySeries

SYNTHETIC_MARKET = UUID("00000000-0000-0000-0000-00000000a001")
SYNTHETIC_COMMODITY = UUID("00000000-0000-0000-0000-00000000c001")
START = date(2026, 1, 1)


def synthetic_series(values: list[str], first_day: date = START, step_days: int = 1) -> DailySeries:
    """A synthetic series with one point every ``step_days`` days carrying the given modal prices."""
    points = tuple(DailyPoint(first_day + timedelta(days=index * step_days), Decimal(value), None, None)
                   for index, value in enumerate(values))
    return DailySeries(SYNTHETIC_MARKET, SYNTHETIC_COMMODITY, "Rs./Quintal", points)

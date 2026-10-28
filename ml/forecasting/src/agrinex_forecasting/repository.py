"""Database access of the ML job, within the privileges of the ``agrinex_ml`` role (ADR-0030 item 1).

The job reads only the ``daily_price_series`` view and writes only ``forecasts``. Each generation is inserted in
one transaction, so a partial generation is never visible; rows are never updated
(docs/api/ML_CONTRACT_BOUNDARY.md section 4, write protocol).
"""

from __future__ import annotations

from collections.abc import Iterator
from itertools import groupby

import psycopg

from agrinex_forecasting.output import ForecastOutput
from agrinex_forecasting.series import DailyPoint, DailySeries

_SERIES_QUERY = """
    SELECT market_id, commodity_id, report_date, modal_price, min_price, max_price, price_unit
    FROM daily_price_series
    ORDER BY market_id, commodity_id, report_date
"""

_INSERT_POINT = """
    INSERT INTO forecasts (market_id, commodity_id, forecast_horizon_days, forecast_date, predicted_price,
                           lower_bound, upper_bound, model_name, model_version, mae, rmse,
                           training_data_through, generated_at)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
"""


class MixedUnitError(ValueError):
    """A series whose points do not share one price unit; the contract forbids it."""


class SeriesReader:
    """Streams the daily series of every market and commodity pair, one pair at a time."""

    def __init__(self, connection: psycopg.Connection) -> None:
        self._connection = connection

    def series(self) -> Iterator[DailySeries | MixedUnitError]:
        with self._connection.transaction(), self._connection.cursor(name="daily_price_series") as cursor:
            cursor.execute(_SERIES_QUERY)
            for (market_id, commodity_id), rows in groupby(cursor, key=lambda row: (row[0], row[1])):
                rows = list(rows)
                units = {row[6] for row in rows}
                if len(units) != 1:
                    yield MixedUnitError(f"Series {market_id}/{commodity_id} mixes price units")
                    continue
                yield DailySeries(market_id, commodity_id, units.pop(),
                                  tuple(DailyPoint(row[2], row[3], row[4], row[5]) for row in rows))


class ForecastWriter:
    """Inserts one generation of forecast points in one transaction."""

    def __init__(self, connection: psycopg.Connection) -> None:
        self._connection = connection

    def write(self, output: ForecastOutput) -> None:
        with self._connection.transaction(), self._connection.cursor() as cursor:
            cursor.executemany(_INSERT_POINT, [
                (output.market_id, output.commodity_id, output.forecast_horizon_days, point.date,
                 point.predicted_price, point.lower_bound, point.upper_bound, output.model_name,
                 output.model_version, output.mae, output.rmse, output.training_data_through, output.generated_at)
                for point in output.forecast
            ])

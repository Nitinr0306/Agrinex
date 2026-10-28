"""The output contract ForecastOutput (contracts/forecast-output.schema.json; ML_CONTRACT_BOUNDARY.md section 4).

One ForecastOutput is one generation: one market, commodity and horizon at one ``generated_at``. Every value is
produced by the job; nothing is filled in. Each generation is validated against the contract schema before it is
written.
"""

from __future__ import annotations

import json
from dataclasses import dataclass
from datetime import date, datetime
from decimal import ROUND_HALF_UP, Decimal
from pathlib import Path
from typing import Any
from uuid import UUID

from jsonschema import Draft202012Validator
from referencing import Registry, Resource

#: Spec section 35: predict 3 or 7 days ahead (forecasts.forecast_horizon_days CHECK).
HORIZONS = (3, 7)

_CENTS = Decimal("0.01")
_METRIC_PLACES = Decimal("0.0001")


@dataclass(frozen=True, slots=True)
class ForecastPoint:
    date: date
    predicted_price: Decimal
    lower_bound: Decimal | None
    upper_bound: Decimal | None


@dataclass(frozen=True, slots=True)
class ForecastOutput:
    market_id: UUID
    commodity_id: UUID
    forecast_horizon_days: int
    forecast: tuple[ForecastPoint, ...]
    model_name: str
    model_version: str
    mae: Decimal | None
    rmse: Decimal | None
    training_data_through: date
    generated_at: datetime

    def to_contract(self) -> dict[str, Any]:
        """The generation as the ForecastOutput JSON document."""
        return {
            "marketId": str(self.market_id),
            "commodityId": str(self.commodity_id),
            "forecastHorizonDays": self.forecast_horizon_days,
            "forecast": [
                {
                    "date": point.date.isoformat(),
                    "predictedPrice": point.predicted_price,
                    "lowerBound": point.lower_bound,
                    "upperBound": point.upper_bound,
                }
                for point in self.forecast
            ],
            "modelName": self.model_name,
            "modelVersion": self.model_version,
            "mae": self.mae,
            "rmse": self.rmse,
            "trainingDataThrough": self.training_data_through.isoformat(),
            "generatedAt": self.generated_at.isoformat().replace("+00:00", "Z"),
        }


def price(value: Decimal) -> Decimal:
    """A predicted price as stored: rounded half-up to 2 decimal places (forecasts.predicted_price numeric(14,2))."""
    return value.quantize(_CENTS, rounding=ROUND_HALF_UP)


def metric(value: Decimal | None) -> Decimal | None:
    """A validation metric as stored: rounded half-up to 4 decimal places (forecasts.mae, rmse numeric(14,4))."""
    return None if value is None else value.quantize(_METRIC_PLACES, rounding=ROUND_HALF_UP)


class ContractValidator:
    """Validates generations against contracts/forecast-output.schema.json and the schemas it references."""

    def __init__(self, contracts_dir: Path) -> None:
        output_schema = _load(contracts_dir / "forecast-output.schema.json")
        common_schema = _load(contracts_dir / "common.schema.json")
        registry = Registry().with_resource("common.schema.json", Resource.from_contents(common_schema))
        self._validator = Draft202012Validator(output_schema, registry=registry)

    def problems(self, output: ForecastOutput) -> list[str]:
        """The contract violations of a generation, empty when it is valid."""
        document = output.to_contract()
        found = [f"{'/'.join(str(part) for part in error.path) or '$'}: {error.message}"
                 for error in self._validator.iter_errors(document)]
        dates = [point.date for point in output.forecast]
        if len(dates) != len(set(dates)):
            found.append("forecast: a date appears more than once in one generation")
        if any(day <= output.training_data_through for day in dates):
            found.append("forecast: every date must be after trainingDataThrough")
        for point in output.forecast:
            if point.lower_bound is not None and point.lower_bound > point.predicted_price:
                found.append("forecast: lowerBound is above predictedPrice")
            if point.upper_bound is not None and point.upper_bound < point.predicted_price:
                found.append("forecast: upperBound is below predictedPrice")
        return found


def _load(path: Path) -> dict[str, Any]:
    with path.open(encoding="utf-8") as handle:
        return json.load(handle)

"""The job and the output contract on SYNTHETIC TEST DATA (tests/synthetic.py). Nothing touches a database."""

from datetime import date, datetime, timezone
from decimal import Decimal
from pathlib import Path

from agrinex_forecasting.evaluation import ValidationWindow
from agrinex_forecasting.job import ForecastJob
from agrinex_forecasting.models import NaiveLastValue
from agrinex_forecasting.output import ContractValidator, ForecastOutput, ForecastPoint
from agrinex_forecasting.repository import MixedUnitError
from agrinex_forecasting.series import MIN_OBSERVATIONS
from synthetic import SYNTHETIC_COMMODITY, SYNTHETIC_MARKET, synthetic_series

CONTRACTS = Path(__file__).resolve().parents[3] / "contracts"
GENERATED_AT = datetime(2026, 10, 5, 6, 0, tzinfo=timezone.utc)


class CollectingWriter:
    def __init__(self):
        self.outputs = []

    def write(self, output):
        self.outputs.append(output)


def job(writer, window=None):
    return ForecastJob(NaiveLastValue(), ContractValidator(CONTRACTS), writer, window)


def test_only_series_with_at_least_90_observations_are_forecast():
    writer = CollectingWriter()
    short = synthetic_series(["100"] * (MIN_OBSERVATIONS - 1))
    enough = synthetic_series(["100"] * MIN_OBSERVATIONS)
    report = job(writer).run([short, enough], GENERATED_AT)
    assert report.series_read == 2 and report.series_eligible == 1
    assert [output.forecast_horizon_days for output in writer.outputs] == [3, 7]


def test_each_horizon_is_one_point_after_the_last_observation_with_no_invented_bound():
    writer = CollectingWriter()
    series = synthetic_series(["1000.005"] * MIN_OBSERVATIONS)
    job(writer).run([series], GENERATED_AT)
    three_days = writer.outputs[0]
    assert three_days.training_data_through == series.last_date
    assert three_days.forecast == (ForecastPoint(date(2026, 4, 3), Decimal("1000.01"), None, None),)
    assert three_days.mae is None and three_days.rmse is None
    assert three_days.model_name == "naive-last-value"


def test_without_a_writer_nothing_is_written():
    report = job(None).run([synthetic_series(["100"] * MIN_OBSERVATIONS)], GENERATED_AT)
    assert report.series_eligible == 1 and report.generations_written == 0


def test_series_mixing_units_are_skipped():
    writer = CollectingWriter()
    report = job(writer).run([MixedUnitError("mixed")], GENERATED_AT)
    assert report.series_skipped_mixed_units == 1 and writer.outputs == []


def test_validation_metrics_are_rounded_to_four_places():
    writer = CollectingWriter()
    values = [str(100 + (index % 3)) for index in range(MIN_OBSERVATIONS)]
    job(writer, ValidationWindow(date(2026, 3, 1), date(2026, 3, 20))).run([synthetic_series(values)], GENERATED_AT)
    assert all(output.mae is not None and -output.mae.as_tuple().exponent <= 4 for output in writer.outputs)


def test_the_contract_rejects_a_generation_that_breaks_it():
    validator = ContractValidator(CONTRACTS)
    output = ForecastOutput(SYNTHETIC_MARKET, SYNTHETIC_COMMODITY, 5,
                            (ForecastPoint(date(2026, 1, 1), Decimal("100.00"), Decimal("120.00"), None),),
                            "naive-last-value", "0.1.0", None, None, date(2026, 1, 2), GENERATED_AT)
    problems = validator.problems(output)
    assert any("forecastHorizonDays" in problem for problem in problems)
    assert any("after trainingDataThrough" in problem for problem in problems)
    assert any("lowerBound" in problem for problem in problems)


def test_the_contract_accepts_a_valid_generation():
    output = ForecastOutput(SYNTHETIC_MARKET, SYNTHETIC_COMMODITY, 7,
                            (ForecastPoint(date(2026, 1, 9), Decimal("100.00"), None, None),),
                            "naive-last-value", "0.1.0", Decimal("1.2500"), None, date(2026, 1, 2), GENERATED_AT)
    assert ContractValidator(CONTRACTS).problems(output) == []

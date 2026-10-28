"""Baseline models on SYNTHETIC TEST DATA (tests/synthetic.py), with hand-computed expectations."""

from datetime import date
from decimal import Decimal

import pytest

from agrinex_forecasting.models import MODELS, NaiveLastValue, RollingMean
from synthetic import synthetic_series


def test_naive_predicts_the_last_observed_value():
    series = synthetic_series(["100", "110", "120"])
    assert NaiveLastValue().predict(series.points, date(2026, 1, 6)) == Decimal("120")


def test_rolling_mean_uses_the_seven_calendar_days_ending_on_the_last_observation():
    # Points on Jan 1..Jan 10: the window is Jan 4..Jan 10, values 4..10 -> mean 7.
    series = synthetic_series([str(value) for value in range(1, 11)])
    assert RollingMean(7).predict(series.points, date(2026, 1, 13)) == Decimal("7")


def test_rolling_mean_averages_only_the_days_that_have_data():
    # Points every 3 days: Jan 1, 4, 7, 10. Window Jan 4..10 holds 200, 300, 400 -> mean 300.
    series = synthetic_series(["100", "200", "300", "400"], step_days=3)
    assert RollingMean(7).predict(series.points, date(2026, 1, 17)) == Decimal("300")


def test_a_forecast_must_be_for_a_later_date():
    series = synthetic_series(["100"])
    with pytest.raises(ValueError):
        NaiveLastValue().predict(series.points, date(2026, 1, 1))


def test_the_job_can_run_exactly_the_two_spec_baselines():
    assert sorted(MODELS) == ["naive-last-value", "rolling-mean-7d"]

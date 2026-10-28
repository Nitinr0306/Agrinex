"""Chronological evaluation on SYNTHETIC TEST DATA (tests/synthetic.py), with hand-computed metrics."""

from datetime import date
from decimal import Decimal

from agrinex_forecasting.evaluation import ValidationWindow, evaluate
from agrinex_forecasting.models import NaiveLastValue
from synthetic import synthetic_series


class RecordingModel:
    """Remembers the newest date it was shown, to prove that no future data leaks into a prediction."""

    name = "recording"

    def __init__(self):
        self.leaks = []

    def predict(self, history, target):
        if history[-1].date >= target:
            self.leaks.append(target)
        return history[-1].modal_price


def test_naive_metrics_are_hand_computed():
    # Values on Jan 1..Jan 6: 100, 110, 100, 130, 120, 120. Horizon 1, origins Jan 1..Jan 5:
    # errors 100-110=-10, 110-100=10, 100-130=-30, 130-120=10, 120-120=0 -> MAE 12, RMSE sqrt(240).
    series = synthetic_series(["100", "110", "100", "130", "120", "120"])
    metrics = evaluate(NaiveLastValue(), series, 1, ValidationWindow(date(2026, 1, 1), date(2026, 1, 5)))
    assert metrics.evaluated_points == 5
    assert metrics.mae == Decimal("12")
    assert metrics.rmse == Decimal(240).sqrt()
    # A naive forecast never moves, so its direction is right only when the price did not move (Jan 5 -> 6).
    assert metrics.directional_accuracy == Decimal("0.2")


def test_origins_outside_the_window_and_missing_targets_are_not_scored():
    series = synthetic_series(["100", "200", "300", "400"], step_days=2)  # Jan 1, 3, 5, 7
    metrics = evaluate(NaiveLastValue(), series, 1, ValidationWindow(date(2026, 1, 1), date(2026, 1, 7)))
    assert metrics.evaluated_points == 0
    assert metrics.mae is None and metrics.rmse is None and metrics.directional_accuracy is None


def test_the_model_never_sees_data_after_the_origin():
    model = RecordingModel()
    series = synthetic_series([str(100 + value) for value in range(30)])
    evaluate(model, series, 3, ValidationWindow(date(2026, 1, 10), date(2026, 1, 25)))
    assert model.leaks == []

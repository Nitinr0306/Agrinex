"""AgriNex optional ML track: short-horizon mandi price forecasting (spec section 35; ADR-0030).

The job reads the database view ``daily_price_series`` and writes ``forecasts`` through the restricted
``agrinex_ml`` role. The backend never calls it. It must not run before the HARD RULE 2 gate and the spec
section 35 criteria are met (docs/api/ML_CONTRACT_BOUNDARY.md section 8).
"""

__version__ = "0.1.0"

# AgriNex forecasting job (optional ML track)

Short-horizon mandi price forecasting for one market and commodity, 3 or 7 days ahead (spec §35). AgriNex works
completely without it (spec P9).

## Gate

The job must not run against real data before the HARD RULE 2 gate and all spec §35 criteria are met:
at least 90 observations per market–commodity pair, the deterministic engine complete **and deployed**, and
team time after the Phase 1 freeze (`docs/api/ML_CONTRACT_BOUNDARY.md` §8). Starting the track is a human
decision. Until then, `GET /api/v1/markets/{marketId}/forecast` answers `available: false`.

## Boundary (ADR-0030)

- Reads only the view `daily_price_series` and writes only `forecasts` (migration `V9`), as the role
  `agrinex_ml`. The migration creates the role without login. When the track starts, an operator enables
  login with a password kept in the deployment environment.
- The backend never calls this job, and nothing calls it. Core flows never read `forecasts`.
- Every generation is validated against `contracts/forecast-output.schema.json` and inserted in one
  transaction. Rows are never updated.
- Nothing is fabricated: no bounds the model does not produce, no metrics that were not computed (they stay
  null), and no training on `docs/data/raw/Agriculture_price_dataset.csv` (ADR-0005 item 3).

## Models

Only the spec §35 baselines, which are always built first:

- `naive-last-value`;
- `rolling-mean-7d`, the mean of the values in the 7 calendar days ending on the last observation.

ARIMA, Prophet and LightGBM are added only when the track starts and real data justifies them.

Each horizon gives one point at *last observation date + horizon*. `trainingDataThrough` is that last date.
Evaluation is chronological with an expanding window over a validation window you give (for example August,
spec §35), and reports MAE, RMSE and directional accuracy.

## Dependencies

Each dependency is pinned exactly:

| Package | Why | Why this one |
|---|---|---|
| `psycopg[binary]` 3.3.6 | Read the view and write `forecasts` | The maintained PostgreSQL adapter for Python 3; wheels for Python 3.14 |
| `jsonschema` 4.26.0 | Validate each generation against the contract schema before writing | The reference JSON Schema implementation for Python; Draft 2020-12 |
| `pytest` 9.1.1 (tests only) | Unit tests | The standard Python test runner |

No numeric library is needed: the baselines and metrics use exact `Decimal` arithmetic.

## Run

Use Python 3.14 (technology baseline) and a virtual environment outside the repository:

```sh
python -m venv <venv>
<venv>/bin/python -m pip install -e ".[test]"
<venv>/bin/python -m pytest
```

The job needs these environment variables: `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `ML_DB_USER`
(`agrinex_ml`) and `ML_DB_PASSWORD`. Credentials are never logged or passed on the command line.

```sh
python -m agrinex_forecasting --model naive-last-value \
    --validation-start 2026-08-01 --validation-end 2026-08-31          # evaluate and report only
python -m agrinex_forecasting --model naive-last-value --write          # also write the generations
```

The tests use only synthetic series, marked as such in `tests/synthetic.py`. They never touch a database.

To run the whole job locally before the gate, use `sandbox/run_sandbox.ps1` (`docs/LOCAL_DEVELOPMENT_GUIDE.md`
section 9.2). It uses a throwaway in-memory database with the real schema and SYNTHETIC prices only
(`sandbox/synthetic_data.sql`), never the real one. On Windows, set `POSTGRES_HOST=127.0.0.1` rather than
`localhost`: Docker Desktop does not answer IPv6 `::1`, and every connection then stalls.

# AgriNex — Local Run and Demo Guide

How to start AgriNex on the project laptop (Windows 11, PowerShell) and show it to someone, with real government prices. Every command below was run on this laptop on 2026-10-15. Values in angle brackets, such as `<your-password>`, are placeholders: replace them, and never paste real secrets into documents or chat.

What runs locally:

| Part | Where it runs | Port |
|---|---|---|
| PostgreSQL 16 (prices, accounts, alerts) | Docker container `agrinex-dev-agrinex-db-1` | 5433 |
| Redis 7 (rate limits, reference cache) | Docker container `agrinex-dev-agrinex-redis-1` | 6379 |
| Backend (Spring Boot, Java 25) | Terminal 2 | 8081 |
| Frontend (Next.js) | Terminal 3 | 3000 |
| Price ingestion (data.gov.in) | Inside the backend: at start, every 6 hours, plus daily and weekly maintenance (section 9.1) | — |
| ML forecasting | Not a service: an optional batch job, run on demand against a throwaway synthetic database (section 9.2) | 5436 while it runs |

Ports 5432 and 8080 are already taken on this laptop: an old prototype database and Jenkins. Never point AgriNex at 5432.

> **Demo or QA?** Sections 1–8 are everything needed to run and show AgriNex. Section 10 is the full test suite for checking a change; it is not needed for a demo.

## 1. One-time prerequisites

| Tool | Needed version | Check |
|---|---|---|
| Docker Desktop | running, Compose v2 | `docker --version`, then `docker compose version` |
| JDK | 25 | `& "C:\Users\DELL\.jdks\jdk-25.0.4.1+1\bin\java.exe" -version` → `openjdk version "25…"` |
| Node.js | 24 | `& "C:\Users\DELL\.agrinex-tools\node-v24.21.0-win-x64\node.exe" --version` → `v24…` |
| Google Chrome | any current | for the demo and the browser tests |

A plain PowerShell window on this laptop finds Java 21 and Node 20 first. Those are the wrong versions for AgriNex. The terminal commands below therefore put JDK 25 and Node 24 first on `PATH`, for that window only. On another computer, use the paths of its own JDK 25 and Node 24 installations.

## 2. One-time environment file

All local settings live in `infra\.env`, which Git ignores. Create it once from the template and fill in every blank:

```powershell
cd C:\CapstoneProject\Agrinex
if (-not (Test-Path infra\.env)) { Copy-Item infra\.env.example infra\.env }
notepad infra\.env
```

Never copy the template over an existing `infra\.env`. The copy blanks every value, and the database then refuses to start (`required variable POSTGRES_PASSWORD is missing a value`). Keep a copy of the filled-in values in a password manager.

| Variable | Value |
|---|---|
| `POSTGRES_DB`, `POSTGRES_USER` | `agrinex` |
| `POSTGRES_PASSWORD` | `<a long random password>` |
| `POSTGRES_HOST`, `POSTGRES_PORT` | `localhost`, `5433` (the template says `5432`; on this laptop that is the old prototype database) |
| `REDIS_PASSWORD` | `<a long random password>` |
| `REDIS_HOST`, `REDIS_PORT`, `REDIS_MAXMEMORY` | `localhost`, `6379`, `128mb` |
| `JWT_SECRET` | `<32 random bytes, base64>` |
| `AGRINEX_PUBLIC_ORIGIN` | `http://localhost:3000` (the frontend's address, also the base of email links) |
| `AGRINEX_BOOTSTRAP_ADMIN_EMAIL`, `AGRINEX_BOOTSTRAP_ADMIN_PASSWORD` | `<admin email>`, `<15–64 characters>`: your admin login |
| `DATA_GOV_API_KEY` | `<your data.gov.in key>` |
| `AGRINEX_EMAIL_PROVIDER` | `resend` (real emails) or `none` (no emails) |
| `RESEND_API_KEY`, `AGRINEX_EMAIL_FROM` | `<Resend key>`, `AgriNex <noreply@<verified-domain>>` |
| `ML_DB_USER`, `ML_DB_PASSWORD` | `agrinex_ml`, leave empty |
| `SPRING_PROFILES_ACTIVE` | `local` (its former value `dev` also works) |

`POSTGRES_PASSWORD` takes effect only when the database volume is first created. On this laptop the volume already exists and holds the real data, so keep the current value.

**Local and production settings are separate:**

| | This laptop | Public deployment (Oracle VM, pending) |
|---|---|---|
| Settings file | `infra\.env`, from `infra\.env.example` | `/opt/agrinex/.env`, only on the VM, created there from `infra/deploy/production.env.example` by `infra/deploy/create_server_env.sh` |
| How the values are loaded | Docker: `--env-file infra/.env` (section 4). Backend: the `Get-Content` block of section 5, into that window only. Frontend: no file, only `AGRINEX_DEV_API_ORIGIN` in its window | Docker Compose reads `/opt/agrinex/.env` and passes the values to the containers |
| Backend profile | `local`, from `SPRING_PROFILES_ACTIVE` in `infra\.env` | `prod`, fixed in `docker-compose.yml`; the file cannot change it |
| Database and Redis | `localhost:5433` and `localhost:6379` (Docker, reachable only from this laptop) | Containers on the VM's private network, never published |
| Database, Redis and JWT secrets | The values in `infra\.env` | Generated on the VM; they never leave it |

A local start cannot reach production: every local address is `localhost`, and the production database and Redis are neither reachable from outside the VM nor protected by any password that exists on this laptop. The two environments do share the data.gov.in key and the Resend account until those keys are replaced after deployment (`docs/operations/DEPLOYMENT_GUIDE.md` section 9).

## 3. Startup order

Start Docker Desktop and wait until it says it is running. Then use three PowerShell windows, in this order:

1. **Terminal 1:** the database and Redis (section 4). You can close it afterwards.
2. **Terminal 2:** the backend (section 5). Keep it open; it shows the backend's log.
3. **Terminal 3:** the frontend (section 6). Keep it open.

Then open http://localhost:3000.

All three must be running before you sign in. If the backend (Terminal 2) is not running, the frontend still opens, but signing in shows "Something went wrong on our side. Please try again." Start Terminal 2 and wait for `Started AgrinexBackendApplication`.

## 4. Terminal 1 — database and Redis

```powershell
cd C:\CapstoneProject\Agrinex
docker compose --env-file infra/.env -f infra/docker-compose.yml up -d
docker compose --env-file infra/.env -f infra/docker-compose.yml ps
```

Both containers must show `(healthy)`, with `127.0.0.1:5433->5432/tcp` and `127.0.0.1:6379->6379/tcp`. This command never deletes data: the volume `agrinex-dev_agrinex-pgdata` keeps everything between restarts.

Check that the database answers and holds real prices:

```powershell
docker exec agrinex-dev-agrinex-db-1 pg_isready -U agrinex -d agrinex
docker exec agrinex-dev-agrinex-db-1 psql -U agrinex -d agrinex -c "select count(*) as observations, min(report_date) as first_day, max(report_date) as latest_day from price_observations"
docker exec agrinex-dev-agrinex-db-1 psql -U agrinex -d agrinex -c "select review_status, count(*) from markets group by 1"
```

Expected: `accepting connections`, more than 400,000 observations from 2025-10-15 to the latest report date, and more than 350 VERIFIED markets.

## 5. Terminal 2 — backend

```powershell
cd C:\CapstoneProject\Agrinex
$env:JAVA_HOME = "C:\Users\DELL\.jdks\jdk-25.0.4.1+1"; $env:Path = "$env:JAVA_HOME\bin;$env:Path"
Get-Content infra\.env | Where-Object { $_ -match '^\s*[A-Za-z_][A-Za-z0-9_]*=' } | ForEach-Object {
    $name, $value = $_ -split '=', 2
    [Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim().Trim('"'), 'Process')
}
$env:SERVER_PORT = '8081'
cd backend
.\mvnw -DskipTests package
java -jar target\agrinex-backend-0.0.1-SNAPSHOT.jar
```

- The `Get-Content` block copies `infra\.env` into this window only. Nothing is printed.
- **Profile:** the backend starts only with exactly one profile. `infra\.env` selects it with `SPRING_PROFILES_ACTIVE`: `local` (its former value `dev` still works and activates `local`). `local` gives the localhost defaults and lets the dev server on port 3000 call the API; without a profile, or with `prod`, the backend stops at once with a message naming what to set.
- `.\mvnw -DskipTests package` builds the backend in about a minute. It is needed the first time and after code changes. On later starts, run only the last command.
- The backend is ready when the log shows `Started AgrinexBackendApplication`, after 20–30 seconds.
- **Migrations run automatically at start.** The log shows `Successfully validated 13 migrations` and either `Successfully applied … migrations` or `Schema "public" is up to date. No migration necessary.` There is no separate migration command.
- At start, the backend also fetches today's prices if it has not succeeded in the last 6 hours. It then repeats every 6 hours (00:00, 06:00, 12:00, 18:00 IST).
- While the backend runs, it also checks the historical report dates every day at 03:00 IST and fetches up to 31 missing or incomplete ones, and on Sundays at 04:00 IST it fetches the last 14 report dates again (ADR-0054). *Admin → Data health* shows which dates are complete. On this laptop these runs happen only if the backend is running at those times; the next run catches up.

**Alternative for development:** `.\mvnw spring-boot:run` replaces the last two lines. It starts more slowly, and it is slower under load because it starts the JVM with `-XX:TieredStopAtLevel=1`.

Check from any window:

```powershell
Invoke-RestMethod http://localhost:8081/actuator/health/readiness | Format-List status
(Invoke-RestMethod http://localhost:8081/api/v1/commodities).items | Select-Object -First 5 name, priceUnit | Format-Table
```

Readiness must say `status : UP`. The commodity list shows the crops that have recent prices.

## 6. Terminal 3 — frontend

```powershell
cd C:\CapstoneProject\Agrinex\frontend
$env:Path = "C:\Users\DELL\.agrinex-tools\node-v24.21.0-win-x64;$env:Path"
npm ci
$env:AGRINEX_DEV_API_ORIGIN = 'http://localhost:8081'
npm run dev
```

- `npm ci` installs the exact dependencies, in about a minute. It is needed only the first time and after `package.json` changes. Its warnings about `install-scripts` are expected.
- The frontend is ready when it prints `Ready`.
- Open **http://localhost:3000**. The frontend forwards `/api/v1` to the backend on 8081, so the browser sees one address.

## 7. URLs and health checks

| What | Address |
|---|---|
| Farmer app | http://localhost:3000 |
| Admin console | http://localhost:3000/admin (sign in with the bootstrap admin) |
| API | http://localhost:8081/api/v1 (for example `/commodities`) |
| Backend readiness (use this one) | http://localhost:8081/actuator/health/readiness |
| Backend liveness | http://localhost:8081/actuator/health/liveness |
| Backend overall health (includes Redis) | http://localhost:8081/actuator/health |
| Frontend health | http://localhost:3000/api/health |

Overall health turns 503 while Redis is down, but the backend keeps working. Use readiness to decide whether AgriNex is usable.

## 8. Demonstration script (about 10 minutes)

All prices are real Government of India (DMI) mandi prices from data.gov.in.

1. **Compare markets.**
   1. On http://localhost:3000 click **Compare markets**.
   2. Choose **District: Nashik**, type **Onion** under Crop and pick it.
   3. Enter **Quantity 20** and select **quintal**, keep **150 km**, then click **Find markets**.

   Show these parts of the result:
   - **Recommended**, with its estimated net realization, the money left after transport;
   - the **Why the highest price is not ranked first** box;
   - the **Markets compared** table: price, report date, freshness badge, transport, net and trend.

   Distances are measured from the district centre (marked "approximate").
2. **Why.** Click **Why?** under any market: the reasons behind its rank.
3. **Trend.** Back on the results, click **Price trend**: the daily reported prices for 7 or 30 days and the direction (rising, falling, stable). Days without a report are left empty, never filled in.
4. **What-If.** Click **Try different numbers (what-if)**. Move the **Transport rate**, **Quantity** or **Distance** slider: every net value and the ranking update at once. **Reset to reported** restores the reported figures.
5. **Languages.** In the header, click **मराठी** or **हिंदी**. The same results appear in Marathi or Hindi; prices keep Latin digits.
6. **Alerts and accounts.**
   1. Click **Price alerts**, then **New price alert**: **At or above a price**, Crop **Onion**, Price **500**, then **Save alert**.
   2. Create an account: a mobile number, an email address you can read, and a password of 8 or more characters.
   3. Click **Create account and save alert**.

   The alert appears under **My alerts**. It triggers after the next ingestion run that brings new Onion prices. With `AGRINEX_EMAIL_PROVIDER=resend`, a confirmation email arrives at the address you entered, in English, Marathi and Hindi, with one link.
7. **Sign out and recovery.**
   1. **Account** → **Sign out**.
   2. **Sign in** → **Forgot password?** sends a reset link to the confirmed email.
   3. The link opens a page to choose a new password and works once.
8. **Admin.** Sign in with the bootstrap admin, then show:
   - **Data health**;
   - **Ingestion runs** (each run's fetched, stored and rejected counts);
   - **Markets** (VERIFIED, PROVISIONAL and MERGED);
   - **Commodities** (each crop's verified unit and its evidence);
   - **Audit log**.

Optional for the demo:

- **Load more history.** *Ingestion runs* → trigger **HISTORICAL_PRICE** with a date range of up to 366 days.
- **Verify a new market.** *Markets* → verify a PROVISIONAL market, only with evidence (ADR-0014).

## 9. Price ingestion and ML forecasting

### 9.1 Price ingestion (automatic and manual)

Ingestion is part of the backend: nothing else needs to run. It fetches Maharashtra prices from the two data.gov.in resources with `DATA_GOV_API_KEY` from `infra\.env`.

**Automatic, while the backend (Terminal 2) runs:**

| When (IST) | What | `triggered_by` |
|---|---|---|
| At start, if no run succeeded in the last 6 hours | Current and recent prices | `STARTUP` |
| 00:00, 06:00, 12:00, 18:00 | Current prices, then the recent historical window | `SCHEDULER` |
| Daily 03:00 | Gap check: re-fetches up to 31 missing or incomplete report dates of the last 366 days | `SCHEDULER` |
| Sunday 04:00 | Reconciliation: re-fetches the last 14 report dates | `SCHEDULER` |

A run happens only if the backend is running at that time; the next one catches up. *Admin → Data health* shows the next gap check and reconciliation.

**Manual:**

- In the browser: sign in as the admin, then *Admin → Data health* → **Trigger a run**: choose **CURRENT_PRICE**, or **HISTORICAL_PRICE** with an optional date range of up to 366 days, then **Trigger run**. The run appears under *Ingestion runs*; open it for its counters.
- From PowerShell (the admin credentials are read from `infra\.env` and never printed):

```powershell
cd C:\CapstoneProject\Agrinex
$e = @{}; Get-Content infra\.env | Where-Object { $_ -match '^\s*[A-Za-z_][A-Za-z0-9_]*=' } | ForEach-Object { $n, $v = $_ -split '=', 2; $e[$n.Trim()] = $v.Trim() }
$login = @{ identifier = $e.AGRINEX_BOOTSTRAP_ADMIN_EMAIL; password = $e.AGRINEX_BOOTSTRAP_ADMIN_PASSWORD } | ConvertTo-Json
$token = (Invoke-RestMethod -Method Post http://localhost:8081/api/v1/auth/login -ContentType 'application/json' -Body $login).accessToken
$run = Invoke-RestMethod -Method Post http://localhost:8081/api/v1/admin/ingestion/trigger -Headers @{ Authorization = "Bearer $token" } -ContentType 'application/json' -Body '{"datasetType":"CURRENT_PRICE"}'
Start-Sleep -Seconds 15
Invoke-RestMethod "http://localhost:8081/api/v1/admin/ingestion/runs/$($run.runId)" -Headers @{ Authorization = "Bearer $token" } | Format-List status, recordsFetched, recordsInserted, duplicatesSkipped, validationErrorsCount
```

`status` must become `SUCCESS`. A run that finds only prices already stored inserts 0 and counts them as duplicates; `validationErrorsCount` counts source records rejected by the unit and validity rules. A second trigger while a run of the same dataset is still running answers 409.

**Check the latest runs** (Terminal 1):

```powershell
docker exec agrinex-dev-agrinex-db-1 psql -U agrinex -d agrinex -c "select triggered_by, status, started_at at time zone 'Asia/Kolkata' as started_ist, records_fetched, records_inserted from ingestion_runs order by started_at desc limit 5"
```

### 9.2 ML forecasting (optional batch job)

There is no ML service to start, and the backend never calls the ML code. The forecasting code (`ml/forecasting`) is an optional batch job. It must not run against real data until all of these hold (spec §35; ADR-0030):

- the HARD RULE 2 gate;
- the Phase 1 freeze and deployment;
- a human decision;
- at least 90 observations per market–crop pair (1,301 VERIFIED pairs qualify; see `docs/data/PHASE1_DATA_AND_ML_READINESS_REPORT.md`).

An offline, read-only evaluation of the baselines on real data before deployment is proposed in ADR-0053 (RD-051). It needs the owner's approval first.

Until then:

- the role `agrinex_ml` cannot log in;
- the `forecasts` table stays empty;
- the app never shows a forecast. `GET /api/v1/markets/{id}/forecast` answers `available: false`.

**Run the whole job locally on synthetic data** (needs Docker and the database of section 4):

```powershell
cd C:\CapstoneProject\Agrinex
powershell -ExecutionPolicy Bypass -File ml\forecasting\sandbox\run_sandbox.ps1
```

The script starts a throwaway PostgreSQL `agrinex-ml-sandbox` on 127.0.0.1:5436 that keeps its data in memory only. It copies the schema, but not the rows, of the development database. It loads 120 days of prices for two markets, all named SYNTHETIC (`ml/forecasting/sandbox/synthetic_data.sql`). It then runs the job as `agrinex_ml`: an evaluation of both baselines, `naive-last-value` and `rolling-mean-7d`, with MAE for 3 and 7 days ahead, then `--write`. It lists the 8 forecasts it wrote and removes the sandbox (`-Keep` keeps it). The real database is only read for its schema: its `agrinex_ml` stays without login and its `forecasts` stay empty.

When the job connects to a database on this laptop, use `POSTGRES_HOST=127.0.0.1`, not `localhost`. With `localhost`, Python tries IPv6 first, which Docker Desktop does not answer, and every connection stalls.

**Unit tests** (synthetic series only):

```powershell
cd C:\CapstoneProject\Agrinex\ml\forecasting
& C:\Users\DELL\.agrinex-tools\venvs\ml-forecasting\Scripts\python.exe -m pytest
```

The venv is outside the repository. On another computer, create one with Python 3.14 as `ml/forecasting/README.md` describes.

## 10. Full QA (not needed for a demo)

| Suite | Command (from the folder shown) | Needs | Last result (2026-10-18) |
|---|---|---|---|
| Backend: unit, integration, migrations, contract | `backend`: `.\mvnw test` | JDK 25 on `PATH` (section 5) and Docker running; about 7 minutes | 157 passed |
| Frontend: unit and component | `frontend`: `npm run test` | Node 24 on `PATH` | 199 passed |
| Frontend: types, lint, production build | `npm run typecheck`, `npm run lint`, `npm run build` | Node 24 | all pass |
| ML: unit tests, and the job end to end on synthetic data | section 9.2 | Docker and section 4 for the end-to-end run | 15 passed; sandbox run wrote 8 forecasts |
| Browser journeys (E2E) | `frontend`: `npm run e2e` | Sections 4–6 running, with the extra settings below | 10 passed |

The backend tests start their own temporary PostgreSQL and Redis in Docker and never touch the real database.

**Browser journeys.** These run in Chrome against the real running stack, with nothing mocked. They cover:

- comparison checked against the API;
- the why, trend and what-if screens;
- alert set-up with account creation;
- sign-in and email-link errors;
- Marathi, Hindi and the 375 px layout;
- the admin console.

Before starting the backend for an E2E run, add one line to Terminal 2, just before `java -jar`:

```powershell
$env:AGRINEX_RATELIMITS_REFRESHPERMINUTE = '1000'   # tests only: many page loads per minute
```

And in Terminal 3, before `npm run e2e`:

```powershell
$env:E2E_ADMIN_IDENTIFIER = '<admin email>'; $env:E2E_ADMIN_PASSWORD = '<admin password>'
```

Each run creates one throwaway farmer account with a Resend test address (`delivered+…@resend.dev`), so no email reaches a person, and records a few comparisons in the local database.

**Evaluation and data-readiness queries.** Both are read-only and run from Terminal 1:

```powershell
cd C:\CapstoneProject\Agrinex
docker cp scripts\run_evaluation.sql agrinex-dev-agrinex-db-1:/tmp/run_evaluation.sql
docker exec agrinex-dev-agrinex-db-1 psql -U agrinex -d agrinex -x -f /tmp/run_evaluation.sql
docker cp scripts\data_readiness.sql agrinex-dev-agrinex-db-1:/tmp/data_readiness.sql
docker exec agrinex-dev-agrinex-db-1 psql -U agrinex -d agrinex -f /tmp/data_readiness.sql
```

- `run_evaluation.sql` is Experiment 1 (spec §36). On a local database its figures come from test comparisons and are not evidence.
- `data_readiness.sql` measures integrity, units, coverage, freshness and ML eligibility.

**Load tests.** Every comparison is recorded (ADR-0029), so a load test against the local database adds thousands of rows. Remove them afterwards, after exporting them, as ADR-0052 item 9 describes, so they never enter Experiment 1 figures.

## 11. Logs

| What | Where |
|---|---|
| Backend | Terminal 2 (one JSON line per event; personal data and tokens are never logged) |
| Frontend | Terminal 3 |
| Database and Redis | `docker compose --env-file infra/.env -f infra/docker-compose.yml logs --tail 50` |
| Ingestion results and rejected records | Admin console → **Ingestion runs** → a run → errors |

## 12. Troubleshooting

| Symptom | Fix |
|---|---|
| `docker` errors such as "cannot connect" | Start Docker Desktop and wait until it is running |
| `java -version` shows 21, or the build fails with "release 25 not supported" | Run the `$env:JAVA_HOME` line of section 5 in that window |
| `node --version` shows v20, or `npm ci` complains about `engines` | Run the `$env:Path` line of section 6 in that window |
| `required variable POSTGRES_PASSWORD is missing a value` (or `REDIS_PASSWORD`), or the backend log says `no password was provided` | `infra\.env` is missing or its values are blank, for example after the template was copied over it. Put your saved values back (section 2). Never invent a new `POSTGRES_PASSWORD`: the database keeps the one it was created with |
| `password authentication failed for user "agrinex"` | `POSTGRES_PASSWORD` in `infra\.env` differs from the one the volume was created with. Without deleting anything, set the role to the file's value: `docker exec -it agrinex-dev-agrinex-db-1 psql -U agrinex -d postgres`, then `ALTER ROLE agrinex PASSWORD '<value from infra\.env>';` |
| `Port 8081 was already in use` | A backend is still running: close it with `Ctrl+C` in its window, or find it with `Get-NetTCPConnection -LocalPort 8081` |
| `Port 3000 is in use by process …, using available port 3001 instead` | Another frontend is still running. Stop it (`Ctrl+C` in its window) and start again: AgriNex must run on http://localhost:3000, the only address the backend accepts for sign-in |
| Backend stops at start: `RESEND_API_KEY is not set` | Set it, or use `AGRINEX_EMAIL_PROVIDER=none` |
| Backend stops at start: `AGRINEX_PUBLIC_ORIGIN` | Use `http://localhost:3000` (no path, no trailing text) |
| Log: `Bootstrap admin credentials are set but an ADMIN exists` | Normal: the admin already exists |
| Signing in says "Something went wrong on our side. Please try again." | The backend is not running: the frontend's forwarding to port 8081 fails. Start Terminal 2 (section 5), wait for `Started AgrinexBackendApplication`, then sign in again |
| Pages say "AgriNex is temporarily unavailable" or "Could not reach AgriNex" | The backend or the database is down: check readiness (section 7) and Terminal 2. While the database is down, requests fail within about 3 seconds |
| `NOAUTH Authentication required` in the log | Redis password missing: rerun the `Get-Content` block of section 5 before starting |
| "Prices for this crop are not reported per quintal" | Expected for crops sold per bundle or per head (for example Coriander leaves, Goat): AgriNex compares only per-quintal prices |
| The comparison finds no markets | Choose a larger distance, or a district and crop with recent prices; check section 4's queries |
| No verification or reset email | `AGRINEX_EMAIL_PROVIDER=none` sends nothing; with `resend`, also check the spam folder |
| Browser tests fail with 429 on `/auth/refresh` | The backend was started without the E2E line of section 10 |
| The frontend window closes by itself | Low memory: close other large programs, then start it again |

## 13. Stop, and restart later

**Stop (data is kept):**

1. `Ctrl+C` in Terminal 3 (frontend) and Terminal 2 (backend).
2. In any window:

```powershell
cd C:\CapstoneProject\Agrinex
docker compose --env-file infra/.env -f infra/docker-compose.yml stop
```

Optionally, close Docker Desktop. Never use `docker compose … down -v` or delete the volume `agrinex-dev_agrinex-pgdata`: that erases every stored price, account and audit record.

**Restart later:**

1. Start Docker Desktop.
2. **Terminal 1:** the `up -d` command of section 4.
3. **Terminal 2:** section 5 without `.\mvnw -DskipTests package`, unless the code changed.
4. **Terminal 3:** section 6 without `npm ci`, unless `package.json` changed.
5. Open http://localhost:3000.

## 14. Optional: the whole stack in Docker

This runs the same images as the public deployment (Nginx, frontend, backend, PostgreSQL, Redis) on this laptop over plain HTTP, to check a change to the images or the edge before deploying. It is not needed for development or a demo. It uses its own Compose project (`agrinex`) and its own empty database volume, so the development database of sections 4–5 is never touched.

```powershell
cd C:\CapstoneProject\Agrinex
$env:AGRINEX_DOMAIN = 'localhost'
docker compose --env-file infra/.env -f docker-compose.yml -f infra/docker-compose.dev.yml up -d --build
```

- Open http://localhost:8088. The backend is also on http://localhost:8090 and the frontend on http://localhost:3010; the database on port 5434 and Redis on 6380 (all on 127.0.0.1 only).
- The new database is empty until it is filled: restore a curated copy as `docs/operations/DEPLOYMENT_GUIDE.md` section 4 step 5 describes, or let ingestion and the daily gap check fill it from data.gov.in.
- Stop with the same command and `stop` instead of `up -d --build`. Never use `down -v`.

The public deployment itself is described in `docs/operations/DEPLOYMENT_GUIDE.md`.

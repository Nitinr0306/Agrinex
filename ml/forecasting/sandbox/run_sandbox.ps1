# AgriNex ML forecasting job, end to end on this laptop WITHOUT real data (spec section 35; ADR-0030; README "Gate").
#
# 1. Starts a throwaway PostgreSQL container "agrinex-ml-sandbox" (127.0.0.1:5436) whose data lives only in memory
#    (tmpfs): no Docker volume is created, and removing the container erases everything.
# 2. Copies the SCHEMA ONLY (no rows) of the development database, so the job meets the real view
#    daily_price_series, the real forecasts table and the real agrinex_ml grants.
# 3. Loads synthetic_data.sql: 2 SYNTHETIC markets x 1 SYNTHETIC crop, 120 days of formula prices.
# 4. Enables login for agrinex_ml INSIDE THE SANDBOX ONLY, with a random password that is never shown.
# 5. Runs the job: evaluation of both baselines over the last 30 days, then --write, and lists the forecasts.
# 6. Removes the sandbox container (keep it with -Keep).
#
# The real database (agrinex-dev-agrinex-db-1, 5433) is only read for its schema; its agrinex_ml role stays NOLOGIN
# and its forecasts table stays empty. Running the job on real prices is a separate human decision (ADR-0053, RD-051).
#
# Usage, from the repository root, with Docker running and the development database up (guide section 4):
#   powershell -ExecutionPolicy Bypass -File ml\forecasting\sandbox\run_sandbox.ps1 [-Keep]
param([switch]$Keep)
$ErrorActionPreference = 'Stop'
$repo = Resolve-Path (Join-Path $PSScriptRoot '..\..\..')
$python = 'C:\Users\DELL\.agrinex-tools\venvs\ml-forecasting\Scripts\python.exe'   # ml/forecasting/README.md
$sandbox = 'agrinex-ml-sandbox'
$devDb = 'agrinex-dev-agrinex-db-1'

function Invoke-Native([scriptblock]$command, [string]$what) {
    & $command
    if ($LASTEXITCODE -ne 0) { throw "$what failed (exit $LASTEXITCODE)" }
}

# A previous sandbox is removed only if it really is the in-memory one this script creates.
$existing = docker ps -a --filter "name=^$sandbox$" --format '{{.Names}}'
if ($existing) {
    $tmpfs = docker inspect --format '{{json .HostConfig.Tmpfs}}' $sandbox
    if ($tmpfs -notmatch '/var/lib/postgresql/data') { throw "$sandbox exists but is not an in-memory sandbox; not touching it" }
    Invoke-Native { docker rm -f $sandbox | Out-Null } 'removing the previous sandbox'
}

$bytes = New-Object byte[] 24
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$adminPassword = -join ($bytes | ForEach-Object { $_.ToString('x2') })
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$mlPassword = -join ($bytes | ForEach-Object { $_.ToString('x2') })

Write-Host 'Starting the in-memory sandbox database on 127.0.0.1:5436 ...'
# "-e POSTGRES_PASSWORD" without a value passes this process's variable, so the password is not on the command line.
$env:POSTGRES_PASSWORD = $adminPassword
Invoke-Native { docker run -d --name $sandbox --tmpfs /var/lib/postgresql/data -e POSTGRES_DB=agrinex_ml_sandbox `
        -e POSTGRES_USER=agrinex -e POSTGRES_PASSWORD -p 127.0.0.1:5436:5432 postgres:16.15 | Out-Null } 'starting the sandbox'
Remove-Item Env:POSTGRES_PASSWORD
for ($i = 0; $i -lt 60; $i++) {
    docker exec $sandbox pg_isready -U agrinex -d agrinex_ml_sandbox -h 127.0.0.1 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { break }
    Start-Sleep -Seconds 1
}
Start-Sleep -Seconds 2

try {
    Write-Host 'Copying the schema (no rows) of the development database ...'
    $schema = Join-Path $env:TEMP 'agrinex-ml-sandbox-schema.sql'
    Invoke-Native { docker exec $devDb pg_dump -U agrinex -d agrinex --schema-only --no-owner -f /tmp/agrinex-schema.sql } 'pg_dump --schema-only'
    Invoke-Native { docker cp "${devDb}:/tmp/agrinex-schema.sql" $schema | Out-Null } 'copying the schema out'
    docker exec $devDb rm -f /tmp/agrinex-schema.sql | Out-Null
    Invoke-Native { docker cp $schema "${sandbox}:/tmp/schema.sql" | Out-Null } 'copying the schema in'
    Invoke-Native { docker cp (Join-Path $PSScriptRoot 'synthetic_data.sql') "${sandbox}:/tmp/synthetic_data.sql" | Out-Null } 'copying the synthetic data in'
    Remove-Item $schema
    Invoke-Native { docker exec $sandbox psql -q -U agrinex -d agrinex_ml_sandbox -c 'CREATE ROLE agrinex_ml NOLOGIN' } 'creating agrinex_ml'
    Invoke-Native { docker exec $sandbox psql -q -U agrinex -d agrinex_ml_sandbox -v ON_ERROR_STOP=1 -f /tmp/schema.sql | Out-Null } 'loading the schema'
    Write-Host 'Loading SYNTHETIC prices ...'
    Invoke-Native { docker exec $sandbox psql -U agrinex -d agrinex_ml_sandbox -v ON_ERROR_STOP=1 -f /tmp/synthetic_data.sql } 'loading synthetic data'
    # The password goes through stdin, never the command line.
    "ALTER ROLE agrinex_ml LOGIN PASSWORD '$mlPassword';" | docker exec -i $sandbox psql -q -U agrinex -d agrinex_ml_sandbox
    if ($LASTEXITCODE -ne 0) { throw 'enabling sandbox login for agrinex_ml failed' }

    # The job's settings (ml/forecasting/README.md). 127.0.0.1, not localhost: on Windows, localhost tries IPv6
    # first, which Docker Desktop does not answer, and every connection then stalls.
    $env:POSTGRES_HOST = '127.0.0.1'; $env:POSTGRES_PORT = '5436'; $env:POSTGRES_DB = 'agrinex_ml_sandbox'
    $env:ML_DB_USER = 'agrinex_ml'; $env:ML_DB_PASSWORD = $mlPassword
    Set-Location (Join-Path $repo 'ml\forecasting')
    $end = (Get-Date).AddDays(-1).ToString('yyyy-MM-dd'); $start = (Get-Date).AddDays(-30).ToString('yyyy-MM-dd')
    foreach ($model in 'naive-last-value', 'rolling-mean-7d') {
        Write-Host "Evaluating $model over $start..$end (MAE per horizon) ..."
        Invoke-Native { & $python -m agrinex_forecasting --model $model --validation-start $start --validation-end $end } "evaluating $model"
    }
    foreach ($model in 'naive-last-value', 'rolling-mean-7d') {
        Write-Host "Writing $model forecasts ..."
        Invoke-Native { & $python -m agrinex_forecasting --model $model --write } "writing $model"
    }
    docker exec $sandbox psql -U agrinex -d agrinex_ml_sandbox -c "SELECT f.model_name, m.canonical_name AS market, f.forecast_horizon_days AS days, f.forecast_date, f.predicted_price FROM forecasts f JOIN markets m ON m.id = f.market_id ORDER BY 1, 2, 3"
}
finally {
    Remove-Item Env:ML_DB_PASSWORD -ErrorAction SilentlyContinue
    if ($Keep) {
        Write-Host "Sandbox kept: docker exec -it $sandbox psql -U agrinex -d agrinex_ml_sandbox  (remove: docker rm -f $sandbox)"
    }
    else {
        docker rm -f $sandbox | Out-Null
        Write-Host 'Sandbox removed (in-memory only; no volume was created).'
    }
}

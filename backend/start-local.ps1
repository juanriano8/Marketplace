<#
.SYNOPSIS
    Levanta el proyecto completo en local: PostgreSQL + API. El panel va aparte.

.DESCRIPTION
    Pensado para tomar capturas o revisar el proyecto sin depender de la nube.
    La base es un PostgreSQL portable que vive en %LOCALAPPDATA%\pg16, no un
    servicio de Windows: por eso, tras reiniciar el PC, hay que volver a
    arrancarlo con este script.

    Deja la API escuchando en http://localhost:8080 y, si se indica -ConPanel,
    tambien arranca el panel en http://localhost:3000.

.PARAMETER ConPanel
    Arranca tambien el panel de Next.js en una ventana aparte.

.PARAMETER SoloBase
    Arranca unicamente PostgreSQL (util para trabajar con la API desde el IDE).

.EXAMPLE
    .\start-local.ps1
    .\start-local.ps1 -ConPanel
#>
param(
    [switch]$ConPanel,
    [switch]$SoloBase
)

$ErrorActionPreference = 'Stop'
$raiz = Split-Path $PSScriptRoot -Parent
$pgbin = Join-Path $env:LOCALAPPDATA 'pg16\pgsql\bin'
$pgdata = Join-Path $env:LOCALAPPDATA 'pg16\data'
$log = Join-Path $env:LOCALAPPDATA 'pg16\server.log'

Write-Host "`n=== Marketplace en local ===" -ForegroundColor Cyan

# ------------------------------------------------------------------ PostgreSQL
if (-not (Test-Path (Join-Path $pgbin 'pg_ctl.exe'))) {
    Write-Host "  [ERROR] No se encuentra PostgreSQL en $pgbin" -ForegroundColor Red
    Write-Host "          Reinstalalo con: design/descargar-imagenes-productos.mjs no lo instala;" -ForegroundColor Yellow
    Write-Host "          descarga https://get.enterprisedb.com/postgresql/postgresql-16.9-1-windows-x64-binaries.zip" -ForegroundColor Yellow
    Write-Host "          y descomprimelo en $env:LOCALAPPDATA\pg16" -ForegroundColor Yellow
    exit 1
}

$escuchando = Get-NetTCPConnection -LocalPort 5432 -State Listen -ErrorAction SilentlyContinue
if ($escuchando) {
    Write-Host "  [ok] PostgreSQL ya escucha en 5432" -ForegroundColor Green
} else {
    Write-Host "  arrancando PostgreSQL..." -ForegroundColor Gray
    & (Join-Path $pgbin 'pg_ctl.exe') -D $pgdata -l $log -o '"-p 5432"' start | Out-Null
    $ok = $false
    for ($i = 1; $i -le 20; $i++) {
        Start-Sleep -Seconds 1
        if (Get-NetTCPConnection -LocalPort 5432 -State Listen -ErrorAction SilentlyContinue) { $ok = $true; break }
    }
    if ($ok) { Write-Host "  [ok] PostgreSQL arrancado" -ForegroundColor Green }
    else {
        Write-Host "  [ERROR] no arranco. Ultimas lineas del log:" -ForegroundColor Red
        if (Test-Path $log) { Get-Content $log -Tail 10 | ForEach-Object { Write-Host ("     " + $_) } }
        exit 1
    }
}

if ($SoloBase) {
    Write-Host "`n  Base lista. Cadena: jdbc:postgresql://localhost:5432/marketplace_db (postgres/postgres)" -ForegroundColor Cyan
    exit 0
}

# ------------------------------------------------------------------ API
$api = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($api) {
    Write-Host "  [ok] La API ya escucha en 8080" -ForegroundColor Green
} else {
    Write-Host "  arrancando la API (tarda ~20 s)..." -ForegroundColor Gray
    $env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot'
    Start-Process -FilePath 'powershell.exe' `
        -ArgumentList '-NoExit', '-ExecutionPolicy', 'Bypass', '-File', (Join-Path $PSScriptRoot 'run.ps1') `
        -WorkingDirectory $PSScriptRoot
    for ($i = 1; $i -le 40; $i++) {
        Start-Sleep -Seconds 2
        $r = curl.exe -s -o NUL -w '%{http_code}' 'http://localhost:8080/actuator/health' --max-time 5
        if ($r -eq '200') { Write-Host "  [ok] API arriba" -ForegroundColor Green; break }
    }
}

# ------------------------------------------------------------------ Panel
if ($ConPanel) {
    $panel = Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue
    if ($panel) {
        Write-Host "  [ok] El panel ya escucha en 3000" -ForegroundColor Green
    } else {
        Write-Host "  arrancando el panel..." -ForegroundColor Gray
        Start-Process -FilePath 'powershell.exe' `
            -ArgumentList '-NoExit', '-Command', 'npm run dev:nofork' `
            -WorkingDirectory (Join-Path $raiz 'frontend')
        Start-Sleep -Seconds 10
    }
}

Write-Host "`n=== Listo ===" -ForegroundColor Cyan
Write-Host "  Panel      : http://localhost:3000" -ForegroundColor White
Write-Host "  API        : http://localhost:8080" -ForegroundColor White
Write-Host "  Swagger    : http://localhost:8080/swagger-ui/index.html" -ForegroundColor White
Write-Host "  Base datos : jdbc:postgresql://localhost:5432/marketplace_db (postgres/postgres)" -ForegroundColor White
Write-Host "`n  Cuentas: admin@marketplace.com / Admin123.  ·  diego.herrera3@marketplace.com / Vendedor123!  ·  tomas.rodriguez1@marketplace.com / Comprador123!" -ForegroundColor Gray
Write-Host "  Para apagar la base: & `"$pgbin\pg_ctl.exe`" -D `"$pgdata`" stop`n" -ForegroundColor Gray

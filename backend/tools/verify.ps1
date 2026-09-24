<#
.SYNOPSIS
    Verifica que el proyecto compila, que la API responde y que los endpoints
    funcionan correctamente contra Cloud SQL.

.DESCRIPTION
    Está pensado para que lo ejecutes antes de presentar. Hace, en orden:
      1. Compila el proyecto y ejecuta los 114 tests.
      2. Comprueba que la aplicación está arrancada y responde.
      3. Recorre los endpoints clave (públicos, seguridad, validación).

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File .\tools\verify.ps1
    powershell -ExecutionPolicy Bypass -File .\tools\verify.ps1 -SkipTests
#>
param(
    [switch]$SkipTests,
    [string]$BaseUrl = 'http://localhost:8080'
)

$ErrorActionPreference = 'Continue'
$backend = Split-Path $PSScriptRoot -Parent
$fail = 0

function Step($text) { Write-Host "`n=== $text ===" -ForegroundColor Cyan }
function Good($text) { Write-Host "  [OK]    $text" -ForegroundColor Green }
function Bad($text)  { Write-Host "  [FALLO] $text" -ForegroundColor Red; $script:fail++ }
function Info($text) { Write-Host "  $text" -ForegroundColor Gray }

Write-Host "==================================================================" -ForegroundColor White
Write-Host " VERIFICACION DEL PROYECTO MARKETPLACE" -ForegroundColor White
Write-Host "==================================================================" -ForegroundColor White

# ---------------------------------------------------------------- 1. JDK
Step '1. Java 21'
$javaHome = $env:JAVA_HOME
if (-not $javaHome -or -not (Test-Path (Join-Path $javaHome 'bin\java.exe'))) {
    foreach ($p in @('C:\Program Files\Eclipse Adoptium\jdk-21*','C:\Program Files\Microsoft\jdk-21*','C:\Program Files\Java\jdk-21*')) {
        $f = Get-Item $p -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($f) { $env:JAVA_HOME = $f.FullName; break }
    }
}
if ($env:JAVA_HOME) {
    $ver = (& "$env:JAVA_HOME\bin\java.exe" -version 2>&1 | Select-Object -First 1) -join ''
    Good "JDK encontrado: $ver"
} else {
    Bad 'No se encontró JDK 21. Instálalo con: winget install EclipseAdoptium.Temurin.21.JDK'
    exit 1
}

# ---------------------------------------------------------------- 2. Tests
if (-not $SkipTests) {
    Step '2. Compilación y tests (114 pruebas)'
    Push-Location $backend
    & .\gradlew.bat test --no-daemon 2>$null | Out-String | Write-Host
    $code = $LASTEXITCODE
    Pop-Location
    if ($code -eq 0) {
        $results = Get-ChildItem "$backend\build\test-results\test\TEST-*.xml" -ErrorAction SilentlyContinue
        $total = 0; $bad = 0
        foreach ($r in $results) {
            [xml]$x = Get-Content $r.FullName
            $total += [int]$x.testsuite.tests
            $bad += [int]$x.testsuite.failures + [int]$x.testsuite.errors
        }
        Good "BUILD SUCCESSFUL - $total tests, $bad fallos"
    } else {
        Bad 'EL BUILD FALLÓ. Revisa la salida de arriba.'
    }
} else {
    Step '2. Tests (omitidos con -SkipTests)'
}

# ---------------------------------------------------------------- 3. API
Step "3. La API responde en $BaseUrl"
$healthFile = Join-Path $env:TEMP 'verify-health.json'
$code = & curl.exe -s -o $healthFile -w '%{http_code}' "$BaseUrl/actuator/health" --max-time 10
if ($code -eq '200') {
    Good "Health: $(Get-Content $healthFile -Raw)"
} else {
    Bad "La API no responde (HTTP $code). Arráncala con:  .\run.ps1"
    Write-Host "`nRESULTADO: $fail problema(s). La app debe estar corriendo para el resto de comprobaciones." -ForegroundColor Yellow
    exit 1
}

# ---------------------------------------------------------------- 4. Endpoints
Step '4. Endpoints clave'
$dir = Join-Path $env:TEMP 'mkverify'; New-Item -ItemType Directory -Force -Path $dir | Out-Null

function Check($name, $method, $path, $expect, $body, $token) {
    $bf = Join-Path $dir 'b.json'; $rf = Join-Path $dir 'r.json'
    $a = @('-s','-o',$rf,'-w','%{http_code}','-X',$method,"$BaseUrl$path",'--max-time','20')
    if ($body) {
        [System.IO.File]::WriteAllText($bf, ($body | ConvertTo-Json -Compress))
        $a += @('-H','Content-Type: application/json','--data-binary',"@$bf")
    }
    if ($token) { $a += @('-H',"Authorization: Bearer $token") }
    $c = [int](& curl.exe @a)
    if ($c -eq $expect) { Good "$name -> $c" } else { Bad "$name -> $c (esperaba $expect)" }
    return (Get-Content $rf -Raw -ErrorAction SilentlyContinue)
}

$pub = Check 'GET /api/v1/products (público)'                    GET   '/api/v1/products?page=0&size=5' 200 $null $null
Check 'GET /api/v1/admin/orders sin token (401)'                 GET   '/api/v1/admin/orders'        401 $null $null | Out-Null
Check 'GET /api/v1/cart sin token (401)'                         GET   '/api/v1/cart'                401 $null $null | Out-Null
Check 'POST /api/v1/seller/products sin token (401)'             POST  '/api/v1/seller/products'     401 @{name='x';price=1;stockQuantity=1;category='x'} $null | Out-Null
Check 'POST register/buyer email inválido (400)'                 POST  '/api/v1/auth/register/buyer' 400 @{email='no-es-email';password='Password123!'} $null | Out-Null

# ---------------------------------------------------------------- 5. Cloud SQL
Step '5. Conexión a Cloud SQL'
$envFile = Join-Path $backend '.env'
if (Test-Path $envFile) {
    $cfg = @{}
    Get-Content $envFile | ForEach-Object { $l = $_.Trim(); if ($l -and -not $l.StartsWith('#') -and $l.Contains('=')) { $i = $l.IndexOf('='); $cfg[$l.Substring(0,$i).Trim()] = $l.Substring($i+1).Trim() } }

    # Comprobación TCP sin Test-NetConnection (evita ruido de Write-Progress)
    $tcpOk = $false
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $async = $client.BeginConnect($cfg['DB_HOST'], [int]$cfg['DB_PORT'], $null, $null)
        $tcpOk = $async.AsyncWaitHandle.WaitOne(5000, $false) -and $client.Connected
        $client.Close()
    } catch { $tcpOk = $false }

    if ($tcpOk) { Good "TCP a $($cfg['DB_HOST']):$($cfg['DB_PORT']) accesible" }
    else { Bad "No se alcanza $($cfg['DB_HOST']):$($cfg['DB_PORT']) - revisa Authorized Networks en Cloud SQL" }

    # Login de prueba: prueba de extremo a extremo del usuario + BCrypt + JWT
    $lr = Check 'POST /api/v1/auth/login (admin)' POST '/api/v1/auth/login' 200 @{email=$cfg['BOOTSTRAP_ADMIN_EMAIL'];password=$cfg['BOOTSTRAP_ADMIN_PASSWORD']} $null
    if ($lr) {
        $tok = ($lr | ConvertFrom-Json).accessToken
        if ($tok) {
            Check 'GET /api/v1/admin/orders con token ADMIN (200)' GET '/api/v1/admin/orders' 200 $null $tok | Out-Null
            Info 'Login OK -> la base de datos, el hash BCrypt y el JWT funcionan'
        }
    }
} else {
    Bad "No existe $envFile - cópialo de .env.example"
}

# ---------------------------------------------------------------- Resumen
Write-Host "`n==================================================================" -ForegroundColor White
if ($fail -eq 0) {
    Write-Host " TODO CORRECTO - el proyecto está listo para la demostración" -ForegroundColor Green
    Write-Host "==================================================================" -ForegroundColor White
    Write-Host "`n  Swagger UI : $BaseUrl/swagger-ui.html"
    Write-Host "  Postman    : backend/postman/Marketplace-API.postman_collection.json"
    Write-Host "  Admin      : ver BOOTSTRAP_ADMIN_EMAIL en backend\.env`n"
} else {
    Write-Host " $fail COMPROBACION(ES) FALLIDA(S)" -ForegroundColor Red
    Write-Host "==================================================================" -ForegroundColor White
}

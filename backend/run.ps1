# ============================================================================
# Arranca la API leyendo las variables de backend/.env
# ----------------------------------------------------------------------------
# Uso:
#   .\run.ps1                 -> perfil cloud (Cloud SQL), puerto 8080
#   .\run.ps1 -Profile local  -> perfil por defecto (PostgreSQL local/Docker)
#   .\run.ps1 -Test           -> sólo compila y ejecuta los tests
# ============================================================================
param(
    [ValidateSet('cloud', 'local')]
    [string]$Profile = 'cloud',

    [int]$Port = 8080,

    [switch]$Test
)

$ErrorActionPreference = 'Stop'
$backend = $PSScriptRoot
Set-Location $backend

# ---- 1. Cargar .env (si existe) ------------------------------------------
$envFile = Join-Path $backend '.env'
if (Test-Path $envFile) {
    Write-Host "Cargando variables desde $envFile" -ForegroundColor Cyan
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith('#') -and $line.Contains('=')) {
            $idx = $line.IndexOf('=')
            $key = $line.Substring(0, $idx).Trim()
            $value = $line.Substring($idx + 1).Trim()
            [Environment]::SetEnvironmentVariable($key, $value, 'Process')
        }
    }
}
else {
    Write-Host "No se encontró .env (copia .env.example como .env si vas a usar Cloud SQL)" -ForegroundColor Yellow
}

# ---- 1b. Perfil local: forzar la base de datos local ---------------------
# El .env apunta a Cloud SQL y las variables de entorno TIENEN PRIORIDAD sobre los valores por
# defecto de application.yml. Sin este bloque, "-Profile local" seguiría intentando conectar a
# Cloud SQL. Aquí se sobrescriben para apuntar al PostgreSQL de docker-compose.
if ($Profile -eq 'local') {
    Write-Host "Perfil local: usando PostgreSQL en localhost (se ignoran los datos de Cloud SQL)" -ForegroundColor Yellow
    $env:DB_HOST     = 'localhost'
    $env:DB_PORT     = '5432'
    $env:DB_NAME     = 'marketplace_db'
    $env:DB_USER     = 'postgres'
    $env:DB_PASSWORD = 'postgres'
    $env:DB_SSLMODE  = 'disable'
    $env:JPA_DDL_AUTO = 'update'
}

# ---- 2. Comprobar JDK 21 --------------------------------------------------
# Si JAVA_HOME no está definido o no contiene un java.exe válido, se busca el JDK 21 instalado.
$javaHome = $env:JAVA_HOME
$javaHomeValid = $false
if ($javaHome -and (Test-Path (Join-Path $javaHome 'bin\java.exe'))) {
    $javaHomeValid = $true
}

if (-not $javaHomeValid) {
    Write-Host "JAVA_HOME no apunta a un JDK válido. Buscando instalaciones de Java 21..." -ForegroundColor Yellow
    $candidates = @(
        'C:\Program Files\Eclipse Adoptium\jdk-21*',
        'C:\Program Files\Microsoft\jdk-21*',
        'C:\Program Files\Java\jdk-21*',
        'C:\Program Files\Amazon Corretto\jdk21*',
        'C:\Program Files\Zulu\zulu-21*'
    )
    foreach ($pattern in $candidates) {
        $found = Get-Item $pattern -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
        if ($found) {
            $env:JAVA_HOME = $found.FullName
            $javaHomeValid = $true
            break
        }
    }
}

if ($javaHomeValid) {
    Write-Host "JAVA_HOME = $env:JAVA_HOME" -ForegroundColor Green
}
else {
    Write-Host ""
    Write-Host "ERROR: no se encontró ningún JDK. El proyecto exige Java 21 (Java 8 NO sirve)." -ForegroundColor Red
    Write-Host "Instálalo con:" -ForegroundColor Red
    Write-Host "  winget install EclipseAdoptium.Temurin.21.JDK" -ForegroundColor Red
    Write-Host "y vuelve a ejecutar este script." -ForegroundColor Red
    exit 1
}

# El proyecto exige Java 21: se lo indicamos a Gradle explícitamente.
$gradleArgs = @()
if ($env:JAVA_HOME) { $gradleArgs += "-Dorg.gradle.java.home=$env:JAVA_HOME" }

# ---- 3. Ejecutar ----------------------------------------------------------
if ($Test) {
    Write-Host "Ejecutando tests..." -ForegroundColor Cyan
    & .\gradlew.bat @gradleArgs test
    exit $LASTEXITCODE
}

$env:SPRING_PROFILES_ACTIVE = $Profile
$env:PORT = $Port
Write-Host "Arrancando Marketplace API  |  perfil=$Profile  puerto=$Port" -ForegroundColor Cyan
Write-Host "Swagger UI:  http://localhost:$Port/swagger-ui.html" -ForegroundColor Green
Write-Host "Health:      http://localhost:$Port/actuator/health" -ForegroundColor Green

& .\gradlew.bat @gradleArgs bootRun

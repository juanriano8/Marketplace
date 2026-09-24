# ============================================================================
# Configuración inicial en un PC nuevo
# ----------------------------------------------------------------------------
# Crea el archivo backend/.env (que no está en Git por contener credenciales)
# a partir de la plantilla y comprueba que el entorno es correcto.
#
# Uso:  powershell -ExecutionPolicy Bypass -File .\setup.ps1
# ============================================================================
$ErrorActionPreference = 'Continue'
$backend = $PSScriptRoot
$envFile = Join-Path $backend '.env'
$exampleFile = Join-Path $backend '.env.example'

Write-Host "==================================================================" -ForegroundColor White
Write-Host " CONFIGURACION INICIAL DEL PROYECTO MARKETPLACE" -ForegroundColor White
Write-Host "==================================================================" -ForegroundColor White

# ---------------------------------------------------------------- 1. JDK
Write-Host "`n[1/3] Java 21" -ForegroundColor Cyan
$jdk = $env:JAVA_HOME
if (-not $jdk -or -not (Test-Path (Join-Path $jdk 'bin\java.exe'))) {
    foreach ($p in @(
        'C:\Program Files\Eclipse Adoptium\jdk-21*',
        'C:\Program Files\Microsoft\jdk-21*',
        'C:\Program Files\Java\jdk-21*',
        'C:\Program Files\Amazon Corretto\jdk21*',
        'C:\Program Files\Zulu\zulu-21*'
    )) {
        $f = Get-Item $p -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
        if ($f) { $env:JAVA_HOME = $f.FullName; break }
    }
}

if ($env:JAVA_HOME) {
    $ver = (& "$env:JAVA_HOME\bin\java.exe" -version 2>&1 | Select-Object -First 1) -join ''
    Write-Host "  [OK] $ver" -ForegroundColor Green
    Write-Host "       JAVA_HOME = $env:JAVA_HOME" -ForegroundColor Gray
} else {
    Write-Host "  [FALTA] No se encontró ningún JDK 21." -ForegroundColor Red
    Write-Host "          Instálalo con:  winget install EclipseAdoptium.Temurin.21.JDK" -ForegroundColor Yellow
    Write-Host "          (Java 8 no sirve: Spring Boot 3 no arranca con él)" -ForegroundColor Yellow
    Write-Host "          Después cierra y reabre la terminal, y vuelve a ejecutar este script." -ForegroundColor Yellow
}

# ---------------------------------------------------------------- 2. .env
Write-Host "`n[2/3] Archivo de configuración .env" -ForegroundColor Cyan

if (Test-Path $envFile) {
    Write-Host "  [OK] Ya existe: $envFile" -ForegroundColor Green
    $cfg = @{}
    Get-Content $envFile | ForEach-Object {
        $l = $_.Trim()
        if ($l -and -not $l.StartsWith('#') -and $l.Contains('=')) {
            $i = $l.IndexOf('='); $cfg[$l.Substring(0, $i).Trim()] = $l.Substring($i + 1).Trim()
        }
    }
    Write-Host "       DB_HOST = $($cfg['DB_HOST'])" -ForegroundColor Gray
    Write-Host "       DB_NAME = $($cfg['DB_NAME'])" -ForegroundColor Gray
    Write-Host "       DB_USER = $($cfg['DB_USER'])" -ForegroundColor Gray
    if ($cfg['DB_PASSWORD'] -match 'cambia|cambiar|XXX|AQUI') {
        Write-Host "  [AVISO] DB_PASSWORD parece un marcador de posición. Edítalo antes de arrancar." -ForegroundColor Yellow
    }
} else {
    if (-not (Test-Path $exampleFile)) {
        Write-Host "  [ERROR] No existe ni .env ni .env.example" -ForegroundColor Red
        exit 1
    }

    Write-Host "  No existe .env (es normal: no está en Git porque contiene credenciales)." -ForegroundColor Yellow
    Write-Host "  Lo creo a partir de .env.example." -ForegroundColor Yellow

    $content = Get-Content $exampleFile -Raw

    # Valores por defecto del proyecto
    $content = $content -replace '(?m)^DB_HOST=.*$',        'DB_HOST=136.112.91.42'
    $content = $content -replace '(?m)^DB_NAME=.*$',        'DB_NAME=marketplace_db'
    $content = $content -replace '(?m)^DB_USER=.*$',        'DB_USER=postgres'
    $content = $content -replace '(?m)^SPRING_PROFILES_ACTIVE=.*$', 'SPRING_PROFILES_ACTIVE=cloud'

    Set-Content -Path $envFile -Value $content -Encoding UTF8
    Write-Host "  [OK] Creado $envFile" -ForegroundColor Green
    Write-Host "       Ya viene con el host, la base y el usuario correctos." -ForegroundColor Gray
    Write-Host "  [ACCION] Abre el archivo y pon tu contraseña en DB_PASSWORD:" -ForegroundColor Yellow
    Write-Host "           notepad `"$envFile`"" -ForegroundColor Yellow
}

# ---------------------------------------------------------------- 3. Red
Write-Host "`n[3/3] Acceso de red a Cloud SQL" -ForegroundColor Cyan
if (Test-Path $envFile) {
    $cfg = @{}
    Get-Content $envFile | ForEach-Object {
        $l = $_.Trim()
        if ($l -and -not $l.StartsWith('#') -and $l.Contains('=')) {
            $i = $l.IndexOf('='); $cfg[$l.Substring(0, $i).Trim()] = $l.Substring($i + 1).Trim()
        }
    }
    if ($cfg['DB_HOST']) {
        $tcpOk = $false
        try {
            $c = New-Object System.Net.Sockets.TcpClient
            $a = $c.BeginConnect($cfg['DB_HOST'], [int]$cfg['DB_PORT'], $null, $null)
            $tcpOk = $a.AsyncWaitHandle.WaitOne(6000, $false) -and $c.Connected
            $c.Close()
        } catch { $tcpOk = $false }

        if ($tcpOk) {
            Write-Host "  [OK] Se alcanza $($cfg['DB_HOST']):$($cfg['DB_PORT'])" -ForegroundColor Green
        } else {
            Write-Host "  [FALLA] No se alcanza $($cfg['DB_HOST']):$($cfg['DB_PORT'])" -ForegroundColor Red
            Write-Host "          La IP pública de ESTE PC debe estar autorizada en Cloud SQL:" -ForegroundColor Yellow
            Write-Host "          Cloud SQL -> tu instancia -> Connections -> Networking ->" -ForegroundColor Yellow
            Write-Host "          Authorized networks -> Add network" -ForegroundColor Yellow
            Write-Host "          Tu IP actual: https://whatismyipaddress.com/" -ForegroundColor Yellow
        }
    }
}

Write-Host "`n==================================================================" -ForegroundColor White
Write-Host " SIGUIENTE PASO" -ForegroundColor White
Write-Host "==================================================================" -ForegroundColor White
Write-Host "  1. Edita backend\.env y pon tu DB_PASSWORD"
Write-Host "  2. Compila y prueba:   .\run.ps1 -Test"
Write-Host "  3. Arranca la API:     .\run.ps1"
Write-Host "  4. Verifica todo:      powershell -ExecutionPolicy Bypass -File .\tools\verify.ps1"
Write-Host "  5. Abre Swagger:       http://localhost:8080/swagger-ui.html`n"

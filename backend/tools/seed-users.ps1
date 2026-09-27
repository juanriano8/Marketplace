# ============================================================================
# Crea los usuarios de prueba del marketplace
# ----------------------------------------------------------------------------
#   5 vendedores  +  10 compradores
#
# Los correos y nombres provienen del dataset de referencia
# (marketplace_datos.csv, hoja "usuarios"), para que los datos de prueba
# coincidan con el modelo de datos del proyecto.
#
# El administrador NO se crea aqui: no existe endpoint publico de registro de
# administradores. Se crea con tools/SeedAdminUser.java.
#
# Uso:  powershell -ExecutionPolicy Bypass -File .\tools\seed-users.ps1
# ============================================================================
$ErrorActionPreference = 'Continue'
$backend = Split-Path $PSScriptRoot -Parent
$base = 'http://localhost:8080'
$dir = Join-Path $env:TEMP 'mkseed'
New-Item -ItemType Directory -Force -Path $dir | Out-Null

Write-Host "==================================================================" -ForegroundColor White
Write-Host " SEMBRADO DE USUARIOS DE PRUEBA" -ForegroundColor White
Write-Host "==================================================================" -ForegroundColor White

# ---- Credenciales del administrador (del .env) ----------------------------
$envFile = Join-Path $backend '.env'
if (-not (Test-Path $envFile)) { Write-Host "ERROR: falta $envFile" -ForegroundColor Red; exit 1 }

$cfg = @{}
Get-Content $envFile | ForEach-Object {
    $l = $_.Trim()
    if ($l -and -not $l.StartsWith('#') -and $l.Contains('=')) {
        $i = $l.IndexOf('='); $cfg[$l.Substring(0, $i).Trim()] = $l.Substring($i + 1).Trim()
    }
}

# ---- Usuarios a crear ----------------------------------------------------
$passwordVendedor = 'Vendedor123!'
$passwordComprador = 'Comprador123!'

$vendedores = @(
    @{ id = 3;  nombre = 'Diego Herrera';   correo = 'diego.herrera3@marketplace.com' },
    @{ id = 4;  nombre = 'Jorge Ramirez';   correo = 'jorge.ramirez4@marketplace.com' },
    @{ id = 9;  nombre = 'Santiago Flores'; correo = 'santiago.flores9@marketplace.com' },
    @{ id = 17; nombre = 'Daniel Herrera';  correo = 'daniel.herrera17@marketplace.com' },
    @{ id = 21; nombre = 'Andres Medina';   correo = 'andres.medina21@marketplace.com' }
)

$compradores = @(
    @{ id = 1;  nombre = 'Tomas Rodriguez';    correo = 'tomas.rodriguez1@marketplace.com' },
    @{ id = 2;  nombre = 'Veronica Medina';    correo = 'veronica.medina2@marketplace.com' },
    @{ id = 6;  nombre = 'Daniela Rodriguez';  correo = 'daniela.rodriguez6@marketplace.com' },
    @{ id = 8;  nombre = 'Isabella Jimenez';   correo = 'isabella.jimenez8@marketplace.com' },
    @{ id = 10; nombre = 'Miguel Torres';      correo = 'miguel.torres10@marketplace.com' },
    @{ id = 11; nombre = 'Natalia Guzman';     correo = 'natalia.guzman11@marketplace.com' },
    @{ id = 12; nombre = 'Juliana Ortiz';      correo = 'juliana.ortiz12@marketplace.com' },
    @{ id = 13; nombre = 'Diana Perez';        correo = 'diana.perez13@marketplace.com' },
    @{ id = 14; nombre = 'Julian Munoz';       correo = 'julian.munoz14@marketplace.com' },
    @{ id = 15; nombre = 'Alejandra Rojas';    correo = 'alejandra.rojas15@marketplace.com' }
)

# ---- Utilidades ----------------------------------------------------------
function CallApi {
    param([string]$Method, [string]$Path, $Body, [string]$Token)
    $bf = Join-Path $dir 'b.json'; $rf = Join-Path $dir 'r.json'
    $a = @('-s', '-o', $rf, '-w', '%{http_code}', '-X', $Method, "$base$Path", '--max-time', '30')
    if ($null -ne $Body) {
        [System.IO.File]::WriteAllText($bf, ($Body | ConvertTo-Json -Compress -Depth 6))
        $a += @('-H', 'Content-Type: application/json', '--data-binary', "@$bf")
    }
    if ($Token) { $a += @('-H', "Authorization: Bearer $Token") }
    $code = [int](& curl.exe @a)
    $raw = Get-Content $rf -Raw -ErrorAction SilentlyContinue
    $json = $null
    if ($raw) { try { $json = $raw | ConvertFrom-Json } catch { } }
    return @{ code = $code; json = $json; raw = $raw }
}

# ---- 1. Login del administrador -----------------------------------------
Write-Host "`n[1/3] Iniciando sesion como administrador..." -ForegroundColor Cyan
$login = CallApi POST '/api/v1/auth/login' @{ email = $cfg['BOOTSTRAP_ADMIN_EMAIL']; password = $cfg['BOOTSTRAP_ADMIN_PASSWORD'] } $null
if ($login.code -ne 200) {
    Write-Host "  ERROR: no se pudo autenticar al administrador (HTTP $($login.code))" -ForegroundColor Red
    Write-Host "  Detalle: $($login.raw)" -ForegroundColor Yellow
    exit 1
}
$adminToken = $login.json.accessToken
Write-Host "  OK: $($login.json.email) ($($login.json.role))" -ForegroundColor Green

# ---- 2. Registrar vendedores y compradores ------------------------------
function Registrar {
    param([string]$Kind, [string]$Correo, [string]$Password)
    $r = CallApi POST "/api/v1/auth/register/$Kind" @{ email = $Correo; password = $Password } $null
    if ($r.code -eq 201) { return @{ estado = 'creado'; userId = $r.json.userId } }
    if ($r.code -eq 400 -and $r.raw -match 'already registered') { return @{ estado = 'ya existia'; userId = $null } }
    return @{ estado = "ERROR $($r.code)"; userId = $null; detalle = $r.raw }
}

Write-Host "`n[2/3] Registrando 5 vendedores y 10 compradores..." -ForegroundColor Cyan
$resultadoV = @()
foreach ($v in $vendedores) {
    $res = Registrar 'seller' $v.correo $passwordVendedor
    $resultadoV += ($v + $res)
    $marca = if ($res.estado -like 'ERROR*') { 'FALLO' } else { 'OK   ' }
    $color = if ($res.estado -like 'ERROR*') { 'Red' } else { 'Green' }
    Write-Host ("  [{0}] vendedor  {1,-46} {2}" -f $marca, $v.correo, $res.estado) -ForegroundColor $color
}

$resultadoC = @()
foreach ($c in $compradores) {
    $res = Registrar 'buyer' $c.correo $passwordComprador
    $resultadoC += ($c + $res)
    $marca = if ($res.estado -like 'ERROR*') { 'FALLO' } else { 'OK   ' }
    $color = if ($res.estado -like 'ERROR*') { 'Red' } else { 'Green' }
    Write-Host ("  [{0}] comprador {1,-46} {2}" -f $marca, $c.correo, $res.estado) -ForegroundColor $color
}

# ---- 3. Verificar los vendedores ---------------------------------------
Write-Host "`n[3/3] Verificando los vendedores (para que puedan publicar)..." -ForegroundColor Cyan
$verificados = 0
foreach ($v in $resultadoV) {
    if ($v.estado -like 'ERROR*') { continue }
    $id = $v.userId
    if (-not $id) {
        $buscar = CallApi GET '/api/v1/admin/sellers?page=0&size=100' $null $adminToken
        $encontrado = $buscar.json.content | Where-Object { $_.email -eq $v.correo } | Select-Object -First 1
        if ($encontrado) { $id = $encontrado.id }
    }
    if (-not $id) { Write-Host ("  [FALLO] no se obtuvo el id de {0}" -f $v.correo) -ForegroundColor Red; continue }

    $r = CallApi PATCH "/api/v1/admin/sellers/$id/verify" @{ approved = $true } $adminToken
    if ($r.code -eq 200) {
        $verificados++
        Write-Host ("  [OK   ] verificado {0}" -f $v.correo) -ForegroundColor Green
    } else {
        Write-Host ("  [FALLO] {0} -> HTTP {1}" -f $v.correo, $r.code) -ForegroundColor Red
    }
}

# ---- Resumen ------------------------------------------------------------
Write-Host "`n==================================================================" -ForegroundColor White
Write-Host (" Vendedores: {0} de {1} verificados" -f $verificados, $vendedores.Count)
Write-Host (" Compradores: {0} de {1}" -f ($resultadoC | Where-Object { $_.estado -notlike 'ERROR*' }).Count, $compradores.Count)
Write-Host "==================================================================" -ForegroundColor White
Write-Host "`nCredenciales: vendedores '$passwordVendedor' | compradores '$passwordComprador'" -ForegroundColor Yellow
Write-Host "Listado completo en USUARIOS_PRUEBA.md`n"

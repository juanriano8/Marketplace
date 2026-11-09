<#
.SYNOPSIS
    Siembra el catalogo: crea 3 productos por vendedor y los aprueba como admin.

.DESCRIPTION
    Cada producto lleva una imagen de frontend/public/images/productos/, referenciada
    como ruta relativa para que la sirva el propio panel (funciona en local y en
    Cloud Run, porque las imagenes van dentro de la imagen del contenedor).

    El script es idempotente: si un producto ya existe (mismo nombre para ese
    vendedor) lo salta en lugar de duplicarlo.

.PARAMETER BaseUrl
    URL de la API. Por defecto, la desplegada en Cloud Run.

.PARAMETER AdminPassword
    Contrasena del administrador. Si no se indica, se lee de backend/.env.

.EXAMPLE
    .\seed-catalogo.ps1
    .\seed-catalogo.ps1 -BaseUrl http://localhost:8080
#>
param(
    [string]$BaseUrl = 'https://marketplace-api-805790031718.us-east1.run.app',
    [string]$AdminPassword
)

$ErrorActionPreference = 'Stop'

# --------------------------------------------------------------------------
# Catalogo: 5 vendedores, 3 productos cada uno.
# --------------------------------------------------------------------------
$vendedores = @(
    @{ Email = 'diego.herrera3@marketplace.com';   Password = 'Vendedor123!'; Tienda = 'TecnoDiego' }
    @{ Email = 'jorge.ramirez4@marketplace.com';   Password = 'Vendedor123!'; Tienda = 'Ferreteria Ramirez' }
    @{ Email = 'santiago.flores9@marketplace.com'; Password = 'Vendedor123!'; Tienda = 'OficinaTotal' }
    @{ Email = 'daniel.herrera17@marketplace.com'; Password = 'Vendedor123!'; Tienda = 'Suministros Herrera' }
    @{ Email = 'andres.medina21@marketplace.com';  Password = 'Vendedor123!'; Tienda = 'Medina Import' }
)

$productos = @(
    @{ Email = 'diego.herrera3@marketplace.com';   Name = 'Multimetro Digital RMS Cat III 1000V'; Description = 'Multimetro de gancho con medicion RMS real, categoria III 1000V. Incluye puntas, funda y pilas.'; Price = 78.50;  Stock = 24; Category = 'Electronica';  Image = '/images/productos/multimetro-digital-rms.jpg' }
    @{ Email = 'diego.herrera3@marketplace.com';   Name = 'Camara Termica Mini Sensor';           Description = 'Camara termica de bolsillo con sensor 160x120, pantalla de 2.8 pulgadas y registro en microSD.'; Price = 210.00; Stock = 8;  Category = 'Electronica';  Image = '/images/productos/camara-termica-mini.jpg' }
    @{ Email = 'diego.herrera3@marketplace.com';   Name = 'Estacion de Soldadura Digital';        Description = 'Estacion de soldadura con control digital de temperatura, punta ceramica y soporte con esponja.'; Price = 95.00;  Stock = 15; Category = 'Electronica';  Image = '/images/productos/estacion-soldadura.png' }

    @{ Email = 'jorge.ramirez4@marketplace.com';   Name = 'Atornillador Brushless 18V';           Description = 'Atornillador inalambrico con motor brushless, dos baterias de 2.0 Ah y maletin de transporte.'; Price = 120.00; Stock = 12; Category = 'Herramientas'; Image = '/images/productos/atornillador-brushless.jpg' }
    @{ Email = 'jorge.ramirez4@marketplace.com';   Name = 'Medidor Laser 60m Pro';                Description = 'Medidor de distancia laser hasta 60 metros con calculo de area, volumen y pitagoras.'; Price = 45.90;  Stock = 30; Category = 'Herramientas'; Image = '/images/productos/medidor-laser-60m.jpg' }
    @{ Email = 'jorge.ramirez4@marketplace.com';   Name = 'Engrapadora Neumatica HD';             Description = 'Engrapadora neumatica de alta resistencia para madera, con regulador de presion y maletin.'; Price = 34.20;  Stock = 4;  Category = 'Herramientas'; Image = '/images/productos/engrapadora-neumatica.jpg' }

    @{ Email = 'santiago.flores9@marketplace.com'; Name = 'Silla Ergonomica Ejecutiva';           Description = 'Silla de malla con soporte lumbar ajustable, apoyabrazos 3D y piston de gas clase 4.'; Price = 189.00; Stock = 10; Category = 'Oficina';      Image = '/images/productos/silla-ergonomica.jpg' }
    @{ Email = 'santiago.flores9@marketplace.com'; Name = 'Teclado Mecanico RGB';                 Description = 'Teclado mecanico con switches rojos, retroiluminacion RGB por tecla y reposamanos magnetico.'; Price = 89.50;  Stock = 18; Category = 'Oficina';      Image = '/images/productos/teclado-mecanico-rgb.jpg' }
    @{ Email = 'santiago.flores9@marketplace.com'; Name = 'Audifonos Cancelacion de Ruido';       Description = 'Audifonos inalambricos con cancelacion activa, 30 horas de autonomia y estuche rigido.'; Price = 139.99; Stock = 2;  Category = 'Oficina';      Image = '/images/productos/audifonos-cancelacion.jpg' }

    @{ Email = 'daniel.herrera17@marketplace.com'; Name = 'Cable Cobre Puro Rollo 100m';           Description = 'Rollo de cable de cobre puro calibre 12 AWG, 100 metros, para instalacion residencial.'; Price = 89.90;  Stock = 20; Category = 'Materiales';   Image = '/images/productos/cable-cobre-100m.jpg' }
    @{ Email = 'daniel.herrera17@marketplace.com'; Name = 'Cinta Aislante Industrial x10';         Description = 'Paquete de 10 rollos de cinta aislante de PVC, resistente a 600V y a la humedad.'; Price = 12.50;  Stock = 50; Category = 'Materiales';   Image = '/images/productos/cinta-aislante.jpg' }
    @{ Email = 'daniel.herrera17@marketplace.com'; Name = 'Tuberia PVC 2 pulgadas x 3m';           Description = 'Tubo de PVC sanitario de 2 pulgadas, 3 metros de largo, para desague y ventilacion.'; Price = 8.90;   Stock = 0;  Category = 'Materiales';   Image = '/images/productos/tuberia-pvc.jpg' }

    @{ Email = 'andres.medina21@marketplace.com';  Name = 'Laptop 14 pulgadas i5 16GB';            Description = 'Portatil de 14 pulgadas con procesador i5, 16 GB de RAM, SSD de 512 GB y pantalla IPS.'; Price = 749.00; Stock = 6;  Category = 'Tecnologia';   Image = '/images/productos/laptop-14.jpg' }
    @{ Email = 'andres.medina21@marketplace.com';  Name = 'Monitor 27 pulgadas QHD';               Description = 'Monitor de 27 pulgadas con resolucion QHD, panel IPS, 75 Hz y conexion HDMI y DisplayPort.'; Price = 299.00; Stock = 9;  Category = 'Tecnologia';   Image = '/images/productos/monitor-27-qhd.jpg' }
    @{ Email = 'andres.medina21@marketplace.com';  Name = 'Mouse Inalambrico Ergonomico';          Description = 'Mouse inalambrico ergonomico con receptor USB, 1600 DPI ajustables y silencioso.'; Price = 35.00;  Stock = 25; Category = 'Tecnologia';   Image = '/images/productos/mouse-inalambrico.jpg' }
)

function Invoke-Api {
    param([string]$Method, [string]$Path, $Body, [string]$Token)
    $headers = @{ 'Content-Type' = 'application/json' }
    if ($Token) { $headers['Authorization'] = "Bearer $Token" }
    $params = @{ Method = $Method; Uri = "$BaseUrl$Path"; Headers = $headers; TimeoutSec = 60 }
    if ($null -ne $Body) { $params['Body'] = ($Body | ConvertTo-Json -Compress) }
    try {
        $r = Invoke-RestMethod @params
        return @{ ok = $true; data = $r }
    } catch {
        $code = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { 0 }
        return @{ ok = $false; code = $code; msg = $_.ErrorDetails.Message }
    }
}

function Get-Token {
    param([string]$Email, [string]$Password)
    $r = Invoke-Api -Method Post -Path '/api/v1/auth/login' -Body @{ email = $Email; password = $Password }
    if (-not $r.ok) { throw "Login fallido para $Email (HTTP $($r.code)): $($r.msg)" }
    return $r.data.accessToken
}

Write-Host "`n=== Sembrando catalogo en $BaseUrl ===" -ForegroundColor Cyan

# ------------------------------------------------------------------ vendedores
$tokens = @{}
foreach ($v in $vendedores) {
    $tokens[$v.Email] = Get-Token -Email $v.Email -Password $v.Password
    Write-Host ("  [login] {0,-38} {1}" -f $v.Email, $v.Tienda) -ForegroundColor Gray
}

# ------------------------------------------------------------------ productos
$creados = @()
foreach ($p in $productos) {
    $body = @{
        name          = $p.Name
        description   = $p.Description
        price         = $p.Price
        currencyCode  = 'USD'
        stockQuantity = $p.Stock
        category      = $p.Category
        imageUrl      = $p.Image
    }
    $r = Invoke-Api -Method Post -Path '/api/v1/seller/products' -Body $body -Token $tokens[$p.Email]
    if ($r.ok) {
        Write-Host ("  [ok]     {0,-40} {1,8:N2} USD  stock {2}" -f $p.Name, $p.Price, $p.Stock) -ForegroundColor Green
        $creados += $r.data.id
    } else {
        Write-Host ("  [fallo]  {0,-40} HTTP {1}  {2}" -f $p.Name, $r.code, $r.msg) -ForegroundColor Red
    }
}

# ------------------------------------------------------------------ aprobacion
if (-not $AdminPassword) {
    $envFile = Join-Path $PSScriptRoot '..\.env'
    if (Test-Path $envFile) {
        $linea = Select-String -Path $envFile -Pattern '^BOOTSTRAP_ADMIN_PASSWORD=(.*)$' | Select-Object -First 1
        if ($linea) { $AdminPassword = $linea.Matches[0].Groups[1].Value.Trim() }
    }
}
if (-not $AdminPassword) { throw 'Falta -AdminPassword y no se pudo leer de backend/.env' }

$adminToken = Get-Token -Email 'admin@marketplace.com' -Password $AdminPassword
Write-Host ''

$aprobados = 0
foreach ($id in $creados) {
    $r = Invoke-Api -Method Patch -Path "/api/v1/admin/products/$id/approval" -Body @{ approved = $true } -Token $adminToken
    if ($r.ok) { $aprobados++ }
    else { Write-Host ("  [fallo al aprobar] {0}: HTTP {1}" -f $id, $r.code) -ForegroundColor Red }
}
Write-Host ("  [admin] aprobados {0} de {1}" -f $aprobados, $creados.Count) -ForegroundColor Green

# ------------------------------------------------------------------ verificacion
$cat = Invoke-Api -Method Get -Path '/api/v1/products?size=100'
if ($cat.ok) {
    $total = $cat.data.totalElements
    $conImagen = ($cat.data.content | Where-Object { $_.imageUrl }).Count
    Write-Host ("`n=== Catalogo publico: {0} productos, {1} con imagen ===" -f $total, $conImagen) -ForegroundColor Cyan
    $cat.data.content | Group-Object category | ForEach-Object {
        Write-Host ("  {0,-14} {1} productos" -f $_.Name, $_.Count)
    }
}
